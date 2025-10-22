package com.acf.careerfinder.controller;

import com.acf.careerfinder.geo.MHLocation;
import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.service.QuestionnaireService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
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

    /** Render gating in the chosen language with pre‑filled values if any. */
    @GetMapping("/gating")
    public String showGating(Model model, HttpSession session) {
        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        LocaleContextHolder.setLocale(toLocale(lang));

        // Maharashtra district list for the dropdown (no state selector).
        List<String> districts = MHLocation.districts();
        // Expose under BOTH names (templates previously used `districtsMH`)
        model.addAttribute("districtsMH", districts);
        model.addAttribute("mhDistricts", districts);

        QuestionnaireForm form = new QuestionnaireForm();
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email != null && !email.isBlank()) {
            Map<String, String> saved = questionnaireService.loadAnswersMap(email);
            // Harmonize legacy/new keys so the UI can pre‑select
            String savedDistrict = coalesce(
                    saved.get("gate.Q25_district"),
                    saved.get("gate.DISTRICT")
            );
            Map<String, String> answers = new LinkedHashMap<>(saved);
            if (savedDistrict != null && !savedDistrict.isBlank()) {
                answers.put("gate.Q25_district", savedDistrict);
                // keep legacy alias visible too
                answers.putIfAbsent("gate.DISTRICT", savedDistrict);
            }
            // Default state (hidden field) if missing
            answers.putIfAbsent("gate.Q25_state", "MH");
            form.setAnswers(answers);
        } else {
            LinkedHashMap<String,String> answers = new LinkedHashMap<>();
            answers.put("gate.Q25_state", "MH");
            form.setAnswers(answers);
        }
        model.addAttribute("form", form);

        return switch (lang) {
            case "hi" -> "gating_hi";
            case "mr" -> "gating_mr";
            default -> "gating_en";
        };
    }

    /** Persist answers as gate.Q1..gate.Q24 + Q25 state/district (and legacy alias). */
    @PostMapping("/gating")
    public String saveGating(@ModelAttribute("form") QuestionnaireForm form, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }

        Map<String, String> incoming = form.getAnswers() == null ? Map.of() : form.getAnswers();
        Map<String, String> onlyGate = new LinkedHashMap<>();

        // Save Q25 (state + district)
        String state = incoming.getOrDefault("gate.Q25_state", "MH");
        if (state != null && !state.isBlank()) {
            onlyGate.put("gate.Q25_state", state);
        }

        String distRaw = coalesce(
                incoming.get("gate.Q25_district"),
                incoming.get("gate.DISTRICT") // legacy form key (if any)
        );
        if (distRaw != null && !distRaw.isBlank()) {
            String canonical = MHLocation.canonicalize(distRaw);
            // New key for downstream logic
            onlyGate.put("gate.Q25_district", canonical);
            // Legacy alias retained for compatibility
            onlyGate.put("gate.DISTRICT", canonical);
        }

        // Persist Q1..Q24 (unchanged mapping)
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

    private static String coalesce(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}