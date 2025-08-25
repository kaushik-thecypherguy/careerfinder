package com.acf.careerfinder.controller;

import com.acf.careerfinder.service.QuestionnaireService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/responses")
public class QuestionnaireResponseController {

    @Autowired
    private QuestionnaireService questionnaireService;

    // Get answers for the logged-in user
    @GetMapping("/me")
    public Map<String, String> getMyAnswers(HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Not logged in");
        }
        return questionnaireService.loadAnswersMap(email);
    }

    // (Optional) Get answers for a specific user (e.g., admin tools)
    @GetMapping
    public Map<String, String> getAnswersByEmail(@RequestParam("email") String email) {
        return questionnaireService.loadAnswersMap(email);
    }

    // Clear all answers for the logged-in user
    @DeleteMapping("/me")
    public void deleteMyAnswers(HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Not logged in");
        }
        questionnaireService.deleteAllForUser(email);
    }
}
