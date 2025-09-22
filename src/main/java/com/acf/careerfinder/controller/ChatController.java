package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.ChatHistory;
import com.acf.careerfinder.service.ChatHistoryService;
import com.acf.careerfinder.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private ChatHistoryService chatHistoryService;

    @PostMapping("/ask")
    public String ask(@RequestHeader("email") String email,
                      @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "").trim();
        if (question.isEmpty()) return "Please provide 'question' in request body.";
        String answer;
        try { answer = chatService.ask(email, question); }
        catch (Exception ex) { return "Error calling OpenAI: " + ex.getMessage(); }

        ChatHistory ch = new ChatHistory();
        ch.setQuestion(question);
        ch.setResponse(answer);
        ch.setDate(LocalDateTime.now());
        ch.setEmail(email);
        chatHistoryService.save(ch);
        return answer;
    }

    @PostMapping("/ask-json")
    public ResponseEntity<String> askJson(@RequestBody Map<String, String> body) {
        String sys = body.getOrDefault("systemPrompt", "").trim();
        String usr = body.getOrDefault("userPrompt", "").trim();
        if (sys.isEmpty() || usr.isEmpty()) {
            return ResponseEntity.badRequest().body("Provide 'systemPrompt' and 'userPrompt'.");
        }
        try {
            String json = chatService.askJson(sys, usr);
            return ResponseEntity.ok(json);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error calling OpenAI: " + ex.getMessage());
        }
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
        return chatHistoryService.findChatIdsByEmail(email);
    }
}