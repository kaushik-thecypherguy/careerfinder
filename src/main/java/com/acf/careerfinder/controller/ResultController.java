package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.Recommendation;
import com.acf.careerfinder.psychometrics.TraitProfile;
import com.acf.careerfinder.psychometrics.ScoringService;
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
    private final ScoringService scoringService;   // ✱ NEW ✱

    public ResultController(QuestionnaireService questionnaireService,
                            RecommendationService recommendationService,
                            ScoringService scoringService) {               // ✱ NEW ✱
        this.questionnaireService = questionnaireService;
        this.recommendationService = recommendationService;
        this.scoringService = scoringService;                              // ✱ NEW ✱
    }

    @GetMapping("/result")
    public String showResult(HttpSession session, Model model) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }
        applyLocaleFromSession(session);

        // Existing: answers → AI narrative
        Map<String, String> answers = questionnaireService.loadAnswersMap(email);
        Recommendation rec = recommendationService.compute(answers);
        model.addAttribute("rec", rec);

        // ✱ NEW: answers + meta → TraitProfile (0–100)
        TraitProfile profile = scoringService.scoreForUser(email);
        model.addAttribute("profile", profile);

        return "result";
    }
}