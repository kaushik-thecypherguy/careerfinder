package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.Recommendation;
import com.acf.careerfinder.service.QuestionnaireService;
import com.acf.careerfinder.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;
import java.util.Map;

@Controller
public class ResultController {

    private static void applyLocaleFromSession(HttpSession session) {
        Object v = session.getAttribute("uiLang");
        String lang = (v instanceof String s) ? s : "en";
        Locale L = switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
        LocaleContextHolder.setLocale(L);
    }

    private final QuestionnaireService questionnaireService;
    private final RecommendationService recommendationService;

    public ResultController(QuestionnaireService questionnaireService,
                            RecommendationService recommendationService) {
        this.questionnaireService = questionnaireService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/result")
    public String showResult(HttpSession session, Model model) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }
        applyLocaleFromSession(session);

        Map<String, String> answers = questionnaireService.loadAnswersMap(email);
        Recommendation rec = recommendationService.compute(answers);
        model.addAttribute("rec", rec);
        return "result";
    }
}