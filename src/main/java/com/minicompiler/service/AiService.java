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
        String response = callAI(prompt);
        return AiResponse.builder()
                .content(response)
                .action(request.action().name())
                .responseTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    private String buildPrompt(AiRequest req) {
        return switch (req.action()) {
            case EXPLAIN_ERROR -> """
                Eres un experto en compiladores y lenguajes de programación.
                El siguiente código fue ejecutado en un minicompilador de stack-based VM:Produjo este error: "%s"

                Explica en español de forma clara y amigable:
                1. Qué significa este error exactamente
                2. Por qué ocurrió en este código
                3. Cómo se puede entender mejor para no cometerlo de nuevo

                Sé conciso pero completo. Usa emojis para hacer la explicación más amigable.
                """.formatted(req.sourceCode(), req.errorMessage());

            case SUGGEST_FIX -> """
                Eres un experto en compiladores. Analiza este código de MiniScript:Error encontrado: "%s"

                Proporciona en español:
                1. El código CORREGIDO y completo listo para copiar
                2. Una explicación breve de qué cambió y por qué
                3. Sugerencias adicionales para mejorar el código

                Formato tu respuesta con secciones claras usando markdown.
                """.formatted(req.sourceCode(), req.errorMessage());

            case GENERATE_CODE -> """
                Eres un experto en el lenguaje MiniScript, que tiene esta sintaxis:
                - Variables: var x = 10; const PI = 3.14;
                - Tipos: int, float, string, bool
                - Funciones: function nombre(param1, param2) { ... return valor; }
                - Control: if (cond) { } else { }, while (cond) { }, for (var i = 0; i < n; i++) { }
                - Operadores: +, -, *, /, %, ** (potencia), ++, --
                - Lógicos: and, or, not (también &&, ||, !)
                - Comparación: ==, !=, <, <=, >, >=
                - Compound: +=, -=, *=, /=
                - print(expresión); — para imprimir
                - Comentarios: // línea, /* bloque */
                - Strings se concatenan con +
                - Recursión soportada

                Genera código MiniScript para: %s

                Proporciona:
                1. El código completo y funcional listo para ejecutar
                2. Una explicación breve de cómo funciona
                3. Ejemplo de qué output producirá

                El código debe estar entre triple backticks.
                """.formatted(req.userPrompt());

            case ANALYZE_CODE -> """
                Eres un experto en compiladores y optimización de código.
                Analiza este código MiniScript profundamente:Proporciona en español un análisis completo que incluya:
                1. 📊 **Complejidad**: Análisis de complejidad temporal y espacial
                2. 🐛 **Bugs potenciales**: Problemas que podrían ocurrir
                3. ⚡ **Optimizaciones**: Mejoras concretas de rendimiento
                4. 🏗️ **Estructura**: Calidad del código y buenas prácticas
                5. 💡 **Versión mejorada**: El código reescrito con todas las mejoras

                Sé detallado y técnico pero claro.
                """.formatted(req.sourceCode());
        };
    }

    @SuppressWarnings("unchecked")
    private String callAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Requeridos por OpenRouter
            headers.add("HTTP-Referer", "https://tu-app.onrender.com");
            headers.add("X-Title", "minicompiler");

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> body = Map.of(
                    "model", "qwen/qwen-3.5-plus",
                    "messages", List.of(message)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENROUTER_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseBody.get("choices");

                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageResp =
                            (Map<String, Object>) choice.get("message");

                    return (String) messageResp.get("content");
                }
            }

            return "Respuesta inválida de la IA: " + responseBody;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("OpenRouter API error BODY: {}", e.getResponseBodyAsString());
            return "Error IA: " + e.getResponseBodyAsString();

        } catch (Exception e) {
            log.error("OpenRouter API error", e);
            return "Error interno: " + e.getMessage();
        }
    }
}