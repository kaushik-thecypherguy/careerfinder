package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.ChatHistory;
import com.acf.careerfinder.service.ChatHistoryService;
import com.acf.careerfinder.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
// for commented code below:
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ChatController {

    // Kept for your earlier snippet (unused but harmless)
    private final RestTemplate restTemplate = new RestTemplate();

    // Active client used by /ask
    private final RestTemplate rest = new RestTemplate();

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiUrl;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String openAiModel;

    @Autowired
    ChatService service;

    @Autowired
    private ChatHistoryService chatHistoryService;

    /*  ─────────────────────────────────────────────────────────────────────────
        Your older snippets are preserved below, still commented out so the file
        compiles. When you need one, just remove the leading // on the method.
        They’re lightly updated to use injected properties (no hardcoded keys).
        ───────────────────────────────────────────────────────────────────────── */

    // @PostMapping("/APicall")
    // public Object getCatFact() {
    //     try {
    //         // Example dummy body (adjust as needed)
    //         Map<String, Object> dummy = Map.of(
    //             "model", openAiModel,
    //             "messages", List.of(Map.of("role", "user", "content", "Say hello")))
    //         ;
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setBearerAuth(openAiApiKey);
    //         headers.setContentType(MediaType.APPLICATION_JSON);
    //         return restTemplate.postForObject(openaiUrl, new HttpEntity<>(dummy, headers), Object.class);
    //     } catch (RestClientException e) {
    //         System.err.println("Error calling OpenAI: " + e.getMessage());
    //         return "Error calling OpenAI. Please try again later.";
    //     }
    // }

    // @PostMapping("/ask1")
    // public ResponseEntity<String> chatWithAI(@RequestBody Map<String, String> request) {
    //     String userMessage = request.get("question");
    //     return service.getChatResponse(userMessage);
    // }

    // private final Map<String, List<Message>> store = new ConcurrentHashMap<>();
    // record Message(String role, String content) {}

    // /* ─── 1️⃣  Conversation cache ───────────────────────────────────────── */
    // private final List<Map<String, Object>> conversation =
    //         new CopyOnWriteArrayList<>(List.of(
    //                 Map.of("role", "system", "content", "You are a helpful assistant.")
    //         ));
    //
    // @PostMapping("/reset")
    // public ResponseEntity<Map<String, Object>> resetConversation() {
    //     conversation.clear();
    //     conversation.add(Map.of("role", "system", "content", "You are a helpful assistant."));
    //     Map<String, Object> ack = Map.of(
    //             "status", "ok",
    //             "message", "Conversation has been reset ✅",
    //             "size", conversation.size()
    //     );
    //     return ResponseEntity.ok(ack);
    // }

    /*  ─────────────────────────────────────────────────────────────────────────
        ACTIVE ENDPOINTS
        ───────────────────────────────────────────────────────────────────────── */

    @PostMapping("/ask")
    public String ask(@RequestHeader("email") String email,
                      @RequestBody Map<String, String> body) {

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return "OpenAI API key not configured. Set OPENAI_API_KEY env var or openai.api.key in application.properties.";
        }

        String question = body.getOrDefault("question", "").trim();
        if (question.isEmpty()) {
            return "Please provide 'question' in request body.";
        }

        // Build request
        Map<String, Object> req = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "user", "content", question)
                )
        );

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String answer;
        try {
            // Call OpenAI
            ResponseEntity<Map> res = rest.postForEntity(openaiUrl, new HttpEntity<>(req, headers), Map.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                return "OpenAI call failed with status: " + res.getStatusCode();
            }

            Object choices = res.getBody().get("choices");
            if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
                return "No choices returned from OpenAI.";
            }
            Object firstChoice = ((List<?>) choices).get(0);
            if (!(firstChoice instanceof Map)) {
                return "Unexpected OpenAI response format.";
            }
            Object msg = ((Map<?, ?>) firstChoice).get("message");
            if (!(msg instanceof Map)) {
                return "Missing message in OpenAI response.";
            }
            Object content = ((Map<?, ?>) msg).get("content");
            answer = content == null ? "" : content.toString();

        } catch (RestClientException ex) {
            return "Error calling OpenAI: " + ex.getMessage();
        }

        // Persist history
        ChatHistory ch = new ChatHistory();
        ch.setQuestion(question);
        ch.setResponse(answer);
        ch.setDate(LocalDateTime.now());
        ch.setEmail(email);

        chatHistoryService.save(ch);

        return answer;
    }

    @GetMapping("/chats/{email}")
    public List<ChatHistory> getChatHistory(@PathVariable String email) {
        return chatHistoryService.getHistory(email);
    }

    @GetMapping("/chats/id/{chatId}")
    public List<ChatHistory> getChatById(@PathVariable int chatId) {
        return chatHistoryService.getHistoryByChatId(chatId);
    }

    @DeleteMapping("/chats/id/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable int chatId) {
        int removed = chatHistoryService.deleteChatByChatId(chatId);
        if (removed == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No chat found with ID: " + chatId);
        } else {
            return ResponseEntity.ok("The chat with ID " + chatId + " was successfully deleted.");
        }
    }

    @GetMapping("/chats/email/{email}/ids")
    public List<Integer> getChatIdsByEmail(@PathVariable String email) {
        return chatHistoryService.getChatIdsForEmail(email);
    }
}
