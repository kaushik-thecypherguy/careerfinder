package com.acf.careerfinder.service;


import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> getChatResponse(String userMessage) {
        String chatApiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        String openAiApiKey = "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";
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "You are a helpful assistant."),
                Map.of("role", "user", "content", userMessage)
        });

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(chatApiUrl, HttpMethod.POST, request, String.class);

        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String content = jsonObject
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return ResponseEntity.ok(content.trim());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to parse response: " + e.getMessage());
        }
    }


}
