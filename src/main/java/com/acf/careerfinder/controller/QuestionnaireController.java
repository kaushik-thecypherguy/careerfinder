package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.service.QuestionBankService;
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
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final QuestionBankService questionBankService;

    public QuestionnaireController(QuestionnaireService questionnaireService,
                                   QuestionBankService questionBankService) {
        this.questionnaireService = questionnaireService;
        this.questionBankService = questionBankService;
    }

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    @GetMapping("/questionnaire")
    public String showQuestionnaire(Model model,
                                    HttpSession session,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {

        // --- NEW: gating guard (do not allow skipping About‑You gating) ---
        if (!Boolean.TRUE.equals(session.getAttribute("GATING_DONE"))) {
            return "redirect:/gating";
        }

        // Always use session language; do not accept an override param here.
        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));

        // Lock language at first entry to questionnaire
        if (!Boolean.TRUE.equals(session.getAttribute("LANG_LOCKED"))) {
            session.setAttribute("LANG_LOCKED", true);
        }

        // Load questions page
        QuestionBankService.PageView pv = questionBankService.loadPage(lang, page, pageSize);
        model.addAttribute("page", pv);

        // Pre-fill saved answers (for persistence across pages/sessions)
        QuestionnaireForm form = new QuestionnaireForm();
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email != null && !email.isBlank()) {
            Map<String, String> saved = questionnaireService.loadAnswersMap(email);
            form.setAnswers(new LinkedHashMap<>(saved));
        }
        model.addAttribute("form", form);

        return "questionnaire_db";
    }

    @PostMapping("/questionnaire/submit")
    public String submit(@ModelAttribute("form") QuestionnaireForm form, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return "redirect:/login?error=loginRequired";
        }
        questionnaireService.saveAnswers(email, form.getAnswers());
        return "redirect:/result";
    }
}