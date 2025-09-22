package com.acf.careerfinder.service;

import com.acf.careerfinder.model.Recommendation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChatService {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiUrl;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String openAiModel;

    /* ------------------- basic helpers you already had ------------------- */

    public String ask(String userId, String prompt) {
        ensureKey();
        Map<String, Object> req = Map.of(
                "model", openAiModel,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        HttpHeaders headers = headers();
        ResponseEntity<Map> res = rest.postForEntity(openaiUrl, new HttpEntity<>(req, headers), Map.class);
        return extractContent(res);
    }

    public String askJson(String systemPrompt, String userPrompt) {
        ensureKey();
        Map<String, Object> req = Map.of(
                "model", openAiModel,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        HttpHeaders headers = headers();
        ResponseEntity<Map> res = rest.postForEntity(openaiUrl, new HttpEntity<>(req, headers), Map.class);
        return extractContent(res);
    }

    public ResponseEntity<String> getChatResponse(String userMessage) {
        try {
            return ResponseEntity.ok(ask("anon", userMessage).trim());
        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error calling OpenAI: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + ex.getMessage());
        }
    }

    /* ------------------- NEW: structured Recommendation ------------------- */

    public Recommendation askRecommendation(Locale locale, Map<String, String> answers) {
        ensureKey();
        String langName = switch (locale.getLanguage()) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            default -> "English";
        };

        String system = """
            You are a concise career counselor. ALWAYS answer in %s.
            Return ONLY a single JSON object. Use EXACT keys:
            - title (string)
            - summary (string, 1–2 sentences)
            - careersTitle (string)
            - suggestedCareers (array of objects) each with:
                - label (string)
                - href (string URL)
            - nextTitle (string)
            - nextSteps (array of strings)
            - answersTitle (string)
            - answersEcho (array of objects) each with:
                - label (string)  # user-friendly question in the same language
                - value (string)  # user's answer in the same language
            Do not include any additional top-level keys.
            """.formatted(langName);

        // Put the raw answers as JSON; the model will translate/label them in answersEcho.
        String answersJson;
        try {
            answersJson = mapper.writeValueAsString(answers == null ? Map.of() : answers);
        } catch (JsonProcessingException e) {
            answersJson = "{}";
        }

        String user = """
            User answers (raw key->value JSON):
            %s

            Create localized labels in "answersEcho". If a URL is relevant for a career, include it in "href".
            """.formatted(answersJson);

        String json = askJson(system, user);

        // Parse, with a light "alias patch" if the model used 'careers' or 'url' anyway.
        try {
            return mapper.readValue(json, Recommendation.class);
        } catch (Exception first) {
            String patched = json
                    .replace("\"careers\"", "\"suggestedCareers\"")
                    .replace("\"url\"", "\"href\"");
            try {
                return mapper.readValue(patched, Recommendation.class);
            } catch (Exception second) {
                // Last resort: return a minimal object with the error message; caller can merge fallback.
                Recommendation r = new Recommendation();
                r.setTitle("Your result");
                r.setSummary("We couldn't generate the full narrative (Bad JSON from OpenAI: "
                        + first.getMessage() + "). Showing your inputs below.");
                return r;
            }
        }
    }

    /* ------------------- internals ------------------- */

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(openAiApiKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private void ensureKey() {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key not configured. Set OPENAI_API_KEY or openai.api.key.");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(ResponseEntity<Map> res) {
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("OpenAI call failed: " + res.getStatusCode());
        }
        Object choices = res.getBody().get("choices");
        if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
            throw new IllegalStateException("No choices in OpenAI response.");
        }
        Object first = ((List<?>) choices).get(0);
        if (!(first instanceof Map)) throw new IllegalStateException("Unexpected response.");
        Object msg = ((Map<?, ?>) first).get("message");
        if (!(msg instanceof Map)) throw new IllegalStateException("Missing message.");
        Object content = ((Map<?, ?>) msg).get("content");
        if (content == null) throw new IllegalStateException("Empty content.");
        return content.toString();
    }
}