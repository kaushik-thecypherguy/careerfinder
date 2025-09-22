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
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                    @RequestParam(value = "lang", required = false) String overrideLang) {
        String lang = (overrideLang != null && !overrideLang.isBlank())
                ? overrideLang
                : (String) session.getAttribute("uiLang");

        if (lang == null || lang.isBlank()) lang = "en";
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));

        // load view model
        QuestionBankService.PageView pv = questionBankService.loadPage(lang, page, pageSize);
        model.addAttribute("page", pv);

        // load saved answers for radio preselecting etc.
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