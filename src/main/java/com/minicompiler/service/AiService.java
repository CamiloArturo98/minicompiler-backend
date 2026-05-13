package com.minicompiler.service;

import com.minicompiler.dto.request.AiRequest;
import com.minicompiler.dto.response.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service that delegates AI requests to the OpenRouter API and maps
 * responses back to {@link AiResponse}.
 *
 * <p><b>Factory Method</b> — {@link #buildHeaders} and {@link #buildRequestBody}
 * centralize HTTP request construction, keeping {@link #callAI} focused on
 * the dispatch and response-parsing logic.
 *
 * <p><b>Template Method</b> — {@link #process} enforces a fixed pipeline:
 * prompt building → AI call → response assembly.
 */
@Slf4j
@Service
public class AiService {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String OPENROUTER_URL    = "https://openrouter.ai/api/v1/chat/completions";
    private static final String AI_MODEL          = "anthropic/claude-haiku-4.5";
    private static final String HTTP_REFERER      = "https://minicompiler-backend.onrender.com";
    private static final String X_TITLE           = "MiniCompiler Studio";
    private static final int    MAX_TOKENS        = 2_048;
    private static final double TEMPERATURE       = 0.7;

    private static final String PLACEHOLDER_NO_CODE  = "(sin código)";
    private static final String PLACEHOLDER_NO_ERROR = "(sin error)";

    private static final String MSG_NO_CONTENT   = "La IA no devolvió contenido.";
    private static final String MSG_UNEXPECTED   = "Respuesta inesperada de la IA.";
    private static final String ERR_CLIENT       = "❌ Error de la IA (%s): %s";
    private static final String ERR_SERVER       = "❌ Error del servidor de IA: %s";
    private static final String ERR_INTERNAL     = "❌ Error interno: %s";

    // =========================================================================
    // Fields
    // =========================================================================

    @Value("${app.ai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // =========================================================================
    // Public API — Template Method
    // =========================================================================

    /**
     * Processes an AI request through the fixed pipeline:
     * prompt building → OpenRouter call → response assembly.
     *
     * @param  request the AI action and context submitted by the user
     * @return the AI-generated response with timing metadata
     */
    public AiResponse process(AiRequest request) {
        long   start  = System.currentTimeMillis();
        var    prompt = buildPrompt(request);
        log.debug("Calling OpenRouter with action={}, promptLength={}", request.action(), prompt.length());
        var    content = callAI(prompt);
        log.debug("OpenRouter response length={}", content.length());
        return AiResponse.builder()
                .content(content)
                .action(request.action().name())
                .responseTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    // =========================================================================
    // Prompt builder
    // =========================================================================

    private String buildPrompt(AiRequest req) {
        var code   = req.sourceCode()   != null ? req.sourceCode()   : PLACEHOLDER_NO_CODE;
        var error  = req.errorMessage() != null ? req.errorMessage() : PLACEHOLDER_NO_ERROR;
        var prompt = req.userPrompt()   != null ? req.userPrompt()   : "";

        return switch (req.action()) {
            case EXPLAIN_ERROR -> """
                Eres un experto en compiladores y lenguajes de programación.
                El siguiente código fue ejecutado en un minicompilador de stack-based VM:%sProdujo este error:"%s"

                Explica en español de forma clara y amigable:
                1. Qué significa este error exactamente
                2. Por qué ocurrió en este código
                3. Cómo se puede entender mejor para no cometerlo de nuevo

                Sé conciso pero completo. Usa emojis para hacer la explicación más amigable.
                """.formatted(code, error);

            case SUGGEST_FIX -> """
                Eres un experto en compiladores. Analiza este código de MiniScript:%sError encontrado:"%s"

                Proporciona en español:
                1. El código CORREGIDO y completo listo para copiar
                2. Una explicación breve de qué cambió y por qué
                3. Sugerencias adicionales para mejorar el código

                Formatea tu respuesta con secciones claras usando markdown.
                """.formatted(code, error);

            case GENERATE_CODE -> """
                Eres un experto en el lenguaje MiniScript, que tiene esta sintaxis:
                - Variables: var x = 10; const PI = 3.14;
                - Tipos: int, float, string, bool
                - Funciones: function nombre(param1, param2) { ... return valor; }
                - Control: if (cond) { } else { }, while (cond) { }, for (var i = 0; i < n; i++) { }
                - Operadores: +, -, *, /, %%, ** (potencia), ++, --
                - Lógicos: and, or, not
                - Comparación: ==, !=, <, <=, >, >=
                - Compound: +=, -=, *=, /=
                - print(expresión); para imprimir
                - Comentarios: // línea
                - Strings se concatenan con +
                - Recursión soportada

                Genera código MiniScript para: %s

                Proporciona:
                1. El código completo y funcional listo para ejecutar (entre triple backticks)
                2. Una explicación breve de cómo funciona
                3. El output esperado al ejecutarlo
                """.formatted(prompt.isBlank()
                    ? "un algoritmo interesante que demuestre las capacidades del lenguaje"
                    : prompt);

            case ANALYZE_CODE -> """
                Eres un experto en compiladores y optimización de código.
                Analiza este código MiniScript profundamente:%sProporciona en español un análisis completo:
                1. 📊 **Complejidad**: Análisis de complejidad temporal y espacial
                2. 🐛 **Bugs potenciales**: Problemas que podrían ocurrir
                3. ⚡ **Optimizaciones**: Mejoras concretas de rendimiento
                4. 🏗️ **Estructura**: Calidad del código y buenas prácticas
                5. 💡 **Versión mejorada**: El código reescrito con todas las mejoras

                Sé detallado y técnico pero claro.
                """.formatted(code);
        };
    }

    // =========================================================================
    // AI call
    // =========================================================================

    @SuppressWarnings("unchecked")
    private String callAI(String prompt) {
        try {
            var entity   = new HttpEntity<>(buildRequestBody(prompt), buildHeaders());
            var response = restTemplate.exchange(OPENROUTER_URL, HttpMethod.POST, entity, Map.class);
            log.debug("OpenRouter status: {}", response.getStatusCode());
            return extractContent((Map<String, Object>) response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("OpenRouter HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ERR_CLIENT.formatted(e.getStatusCode(), e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            log.error("OpenRouter server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ERR_SERVER.formatted(e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("OpenRouter unexpected error: {}", e.getMessage(), e);
            return ERR_INTERNAL.formatted(e.getMessage());
        }
    }

    // =========================================================================
    // Factory Methods — HTTP request construction
    // =========================================================================

    /** Builds the authorization and content-type headers for the OpenRouter request. */
    private HttpHeaders buildHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.add("HTTP-Referer", HTTP_REFERER);
        headers.add("X-Title",      X_TITLE);
        return headers;
    }

    /** Builds the JSON request body with model, messages, and sampling parameters. */
    private Map<String, Object> buildRequestBody(String prompt) {
        var body = new HashMap<String, Object>();
        body.put("model",     AI_MODEL);
        body.put("messages",  List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", MAX_TOKENS);
        body.put("temperature", TEMPERATURE);
        return body;
    }

    // =========================================================================
    // Response parsing
    // =========================================================================

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null || !responseBody.containsKey("choices")) {
            log.warn("Unexpected OpenRouter response: {}", responseBody);
            return MSG_UNEXPECTED;
        }
        var choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices.isEmpty()) return MSG_UNEXPECTED;

        var content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        log.debug("Got content from OpenRouter, length={}", content != null ? content.length() : 0);
        return content != null ? content : MSG_NO_CONTENT;
    }
}