package com.acf.careerfinder.controller;

import com.acf.careerfinder.geo.MHLocation;
import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.model.UserProgress.Section;
import com.acf.careerfinder.service.ProgressService;
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
    private final ProgressService progress;

    public GatingController(QuestionnaireService questionnaireService, ProgressService progress) {
        this.questionnaireService = questionnaireService;
        this.progress = progress;
    }

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    @GetMapping("/gating")
    public String showGating(Model model, HttpSession session) {
        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        LocaleContextHolder.setLocale(toLocale(lang));

        List<String> districts = MHLocation.districts();
        model.addAttribute("districtsMH", districts);
        model.addAttribute("mhDistricts", districts);

        String email = (String) session.getAttribute("USER_EMAIL");
        QuestionnaireForm form = new QuestionnaireForm();
        LinkedHashMap<String,String> answers = new LinkedHashMap<>();

        if (email != null && !email.isBlank()) {
            // persisted
            answers.putAll(questionnaireService.loadAnswersMap(email));
            // in-progress (if any)
            progress.load(email, Section.GATING).ifPresent(up ->
                    answers.putAll(progress.toMap(up.getAnswersJson()))
            );
        }
        answers.putIfAbsent("gate.Q25_state", "MH");

        form.setAnswers(answers);
        model.addAttribute("form", form);

        return switch (lang) {
            case "hi" -> "gating_hi";
            case "mr" -> "gating_mr";
            default -> "gating_en";
        };
    }

    @PostMapping("/gating")
    public String saveGating(@ModelAttribute("form") QuestionnaireForm form, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login?error=loginRequired";

        Map<String, String> incoming = form.getAnswers() == null ? Map.of() : form.getAnswers();
        Map<String, String> onlyGate = new LinkedHashMap<>();

        String state = incoming.getOrDefault("gate.Q25_state", "MH");
        if (state != null && !state.isBlank()) onlyGate.put("gate.Q25_state", state);

        String distRaw = coalesce(incoming.get("gate.Q25_district"), incoming.get("gate.DISTRICT"));
        if (distRaw != null && !distRaw.isBlank()) {
            String canonical = MHLocation.canonicalize(distRaw);
            onlyGate.put("gate.Q25_district", canonical);
            onlyGate.put("gate.DISTRICT", canonical);
        }

        for (int i = 1; i <= 24; i++) {
            String k = "gate.Q" + i;
            String v = incoming.get(k);
            if (v != null) onlyGate.put(k, v);
        }

        String age = incoming.get("gate.Q26_age");
        if (age != null && !age.trim().isEmpty()) onlyGate.put("gate.Q26_age", age.trim());

        // Save canonical gating answers
        questionnaireService.saveAnswers(email, onlyGate);

        // Touch/mark progress so /resume behaves deterministically
        progress.upsertMerge(email, Section.GATING, Map.of(), 1);
        progress.markCompleted(email, Section.GATING);

        return "redirect:/questionnaire?page=1";
    }

    private static String coalesce(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}