package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.Recommendation;
import com.acf.careerfinder.psychometrics.ScoringService;
import com.acf.careerfinder.psychometrics.Trait;
import com.acf.careerfinder.psychometrics.TraitProfile;
import com.acf.careerfinder.sector.SectorRankingService;
import com.acf.careerfinder.service.QuestionnaireService;
import com.acf.careerfinder.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
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
    private final ScoringService scoringService;
    private final SectorRankingService sectorRankingService;

    public ResultController(QuestionnaireService questionnaireService,
                            RecommendationService recommendationService,
                            ScoringService scoringService,
                            SectorRankingService sectorRankingService) {
        this.questionnaireService = questionnaireService;
        this.recommendationService = recommendationService;
        this.scoringService = scoringService;
        this.sectorRankingService = sectorRankingService;
    }

    @GetMapping("/result")
    public String showResult(HttpSession session, Model model) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }
        applyLocaleFromSession(session);

        // Load all saved answers (includes gate.Q25_state/district + gate.Q1..Q24 + psychometric answers)
        Map<String, String> answers = questionnaireService.loadAnswersMap(email);

        // STRICT GUARD: gating must be fully completed (DISTRICT + Q1..Q24 present & non-blank)
        if (!hasAllGateAnswers(answers)) {
            return "redirect:/gating?error=incomplete";
        }

        // Narrative (existing)
        Recommendation rec = recommendationService.compute(answers);
        model.addAttribute("rec", rec);

        // Step‑5: Trait profile
        TraitProfile profile = scoringService.scoreForUser(email);
        model.addAttribute("profile", profile);

        // Convert Trait→Double map to "T01".."T12" → Double for the sector scorer
        Map<String, Double> tCodeScores = toTCodeMap(profile.traitFinal0to100());
        // Phase‑7 compositor → Top‑5 Eligible + Near‑Miss (ineligible ≥ cutoff)
        var ranked = sectorRankingService.build(answers, tCodeScores);

        model.addAttribute("topEligible", ranked.topEligible);
        model.addAttribute("nearMiss", ranked.nearMiss);
        model.addAttribute("eligibleCutoff", ranked.eligibleCutoff);

        return "result";
    }

    private static boolean hasAllGateAnswers(Map<String, String> answers) {
        if (answers == null) return false;

        // Require Q25 district (accept new key or legacy alias)
        String dist = coalesce(answers.get("gate.Q25_district"), answers.get("gate.DISTRICT"));
        if (dist == null || dist.trim().isEmpty()) return false;

        // Require Q1..Q24
        for (int i = 1; i <= 24; i++) {
            String v = answers.get("gate.Q" + i);
            if (v == null || v.trim().isEmpty()) return false;
        }
        return true;
    }

    /** Local helper: the weights JSON uses keys "T01".."T12". */
    private static Map<String, Double> toTCodeMap(Map<Trait, Double> traitMap) {
        Map<String, Double> out = new LinkedHashMap<>();
        int i = 1;
        for (Trait t : Trait.values()) {
            String code = (i < 10) ? ("T0" + i) : ("T" + i);
            double val = traitMap != null && traitMap.get(t) != null ? traitMap.get(t) : 0.0;
            out.put(code, val);
            i++;
        }
        return out;
    }

    private static String coalesce(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
