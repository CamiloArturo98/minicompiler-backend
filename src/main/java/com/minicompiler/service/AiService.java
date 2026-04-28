package com.minicompiler.service;

import com.minicompiler.dto.request.AiRequest;
import com.minicompiler.dto.response.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiService {

    @Value("${app.ai.api-key}")
    private String apiKey;

    private static final String OPENROUTER_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    public AiResponse process(AiRequest request) {
        long start = System.currentTimeMillis();
        String prompt = buildPrompt(request);
        log.debug("Calling OpenRouter with action={}, promptLength={}", request.action(), prompt.length());
        String response = callAI(prompt);
        log.debug("OpenRouter response length={}", response.length());
        return AiResponse.builder()
                .content(response)
                .action(request.action().name())
                .responseTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    private String buildPrompt(AiRequest req) {
        String code  = req.sourceCode()   != null ? req.sourceCode()   : "(sin código)";
        String error = req.errorMessage() != null ? req.errorMessage() : "(sin error)";
        String prompt = req.userPrompt()  != null ? req.userPrompt()   : "";

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
                """.formatted(prompt.isBlank() ? "un algoritmo interesante que demuestre las capacidades del lenguaje" : prompt);

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

    @SuppressWarnings("unchecked")
    private String callAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.add("HTTP-Referer", "https://minicompiler-backend.onrender.com");
            headers.add("X-Title", "MiniCompiler Studio");

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> body = new HashMap<>();
            body.put("model","anthropic/claude-haiku-4.5");
            body.put("messages", List.of(message));
            body.put("max_tokens", 2048);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.debug("Sending request to OpenRouter...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENROUTER_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.debug("OpenRouter status: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> msg = (Map<String, Object>) choice.get("message");
                    String content = (String) msg.get("content");
                    log.debug("Got content from OpenRouter, length={}", content != null ? content.length() : 0);
                    return content != null ? content : "La IA no devolvió contenido.";
                }
            }

            log.warn("Unexpected OpenRouter response: {}", responseBody);
            return "Respuesta inesperada de la IA.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("OpenRouter HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "❌ Error de la IA (" + e.getStatusCode() + "): " + e.getResponseBodyAsString();

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("OpenRouter server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "❌ Error del servidor de IA: " + e.getResponseBodyAsString();

        } catch (Exception e) {
            log.error("OpenRouter unexpected error: {}", e.getMessage(), e);
            return "❌ Error interno: " + e.getMessage();
        }
    }
}