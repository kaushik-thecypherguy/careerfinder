package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.ChatHistory;
import com.acf.careerfinder.repository.ChatRepository;
import com.acf.careerfinder.service.ChatHistoryService;
import com.acf.careerfinder.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ChatController {


    private final RestTemplate restTemplate = new RestTemplate();


    @Autowired
    ChatService service;

    @Autowired
    private ChatHistoryService chatHistoryService;

    //@PostMapping("/APicall")
    /*public Object getCatFact() {
        try {
            String dummyRequestBody = "{ \"dummy\": \"data\" }";

            return restTemplate.postForObject(
                    "https://api.openai.com/v1/chat/completions",
                    dummyRequestBody,
                    Object.class
            );
        } catch (RestClientException e) {
            System.err.println("Error calling OpenAi: " + e.getMessage());
            return "Error calling OpenAi. Please try again later.";
        }
    }*/

    //@PostMapping("/ask1")
    /*public ResponseEntity<String> chatWithAI(@RequestBody Map<String, String> request) {
        String userMessage = request.get("question");
        return service.getChatResponse(userMessage);
    }*/

    //private final Map<String, List<Message>> store = new ConcurrentHashMap<>();
    private final RestTemplate rest = new RestTemplate();
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    String openAiApiKey = "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";


    @PostMapping("/ask")
    public String ask(@RequestHeader("email") String email,
                      @RequestBody Map<String, String> body) {

        // 1. Pull history (or create new)
       // List<Message> history = store.computeIfAbsent(email, k -> new ArrayList<>());

        // 2. Add latest user turn
        //history.add(new Message("user", body.get("question")));

        // 3. Build request
        Map<String, Object> req = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", body.get("question")
                        )
                )
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);


        //username,chatID,question,response and date


        // 4. Call OpenAI
        ResponseEntity<Map> res = rest.postForEntity(
                OPENAI_URL,
                new HttpEntity<>(req, headers),
                Map.class
        );

        // 5. Extract assistant text
        String answer = ((Map) ((Map) ((List) res.getBody().get("choices")).get(0)).get("message"))
                .get("content").toString();

        // 6. Append assistant turn to in-memory history
       // history.add(new Message("assistant", answer));

        // 7. Persist this turn
        ChatHistory ch = new ChatHistory();// or generate however you like
        ch.setQuestion(body.get("question"));
        ch.setResponse(answer);
        ch.setDate(LocalDateTime.now());
        ch.setEmail(email);// make sure the request body carries "email"

        chatHistoryService.save(ch);                  // <- writes to DB

        return answer;
    }


//    record Message(String role, String content) {
//    }


    /* ─── 1️⃣  Conversation cache ─────────────────────────────────────────────── */
//    private final List<Map<String, Object>> conversation =
//            new CopyOnWriteArrayList<>(List.of(
//                    Map.of("role", "system",
//                            "content", "You are a helpful assistant.")
//            ));

    //@PostMapping("/reset")          // POST so no caching proxies get in the way
    /*public ResponseEntity<Map<String, Object>> resetConversation() {

        conversation.clear();                       // wipe everything
        conversation.add(Map.of(                    // re-seed with system prompt
                "role", "system",
                "content", "You are a helpful assistant."
        ));

        Map<String, Object> ack = Map.of(           // confirmation payload
                "status", "ok",
                "message", "Conversation has been reset ✅",
                "size", conversation.size()     // ⇒ 1
        );
        return ResponseEntity.ok(ack);
    }*/


    //Username,UserData Password, ID for that user

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



