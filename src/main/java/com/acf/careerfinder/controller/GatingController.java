package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.service.QuestionnaireService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class GatingController {

    private final QuestionnaireService questionnaireService;

    public GatingController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    /** Render gating in the chosen language (en/hi/mr) with pre-filled values if any. */
    @GetMapping("/gating")
    public String showGating(Model model, HttpSession session) {
        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        LocaleContextHolder.setLocale(toLocale(lang)); // small explicitness

        // Pre-fill previously saved gate.* answers if the user is logged in
        QuestionnaireForm form = new QuestionnaireForm();
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email != null && !email.isBlank()) {
            Map<String, String> saved = questionnaireService.loadAnswersMap(email);
            form.setAnswers(new LinkedHashMap<>(saved)); // template will bind only gate.* keys
        } else {
            form.setAnswers(new LinkedHashMap<>());
        }
        model.addAttribute("form", form);

        return switch (lang) {
            case "hi" -> "gating_hi";
            case "mr" -> "gating_mr";
            default -> "gating_en";
        };
    }

    /** Persist answers as gate.Q1..gate.Q24 and continue. */
    @PostMapping("/gating")
    public String saveGating(@ModelAttribute("form") QuestionnaireForm form, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }
        // Persist only gate.* keys (ignore anything else that might ride along)
        Map<String, String> incoming = form.getAnswers() == null ? Map.of() : form.getAnswers();
        Map<String, String> onlyGate = new LinkedHashMap<>();
        for (int i = 1; i <= 24; i++) {
            String k = "gate.Q" + i;
            String v = incoming.get(k);
            if (v != null) onlyGate.put(k, v);
        }
        questionnaireService.saveAnswers(email, onlyGate);

        // Mark flow checkpoint and continue to page‑1 of questionnaire
        session.setAttribute("GATING_DONE", true);
        return "redirect:/questionnaire?page=1";
    }
}