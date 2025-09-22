package com.acf.careerfinder.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
public class LanguageController {

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    /** New combined page: language select + video on same screen. */
    @GetMapping({"/start", "/language"}) // keep /language for backward compatibility
    public String showStart(HttpSession session) {
        // Page reads session.uiLang to preselect; no redirection needed here.
        return "start";
    }

    /** Stores the chosen language in the session and sets request-locale. */
    @PostMapping("/language/set")
    public String setLanguage(@RequestParam("lang") String lang, HttpSession session) {
        if (!"hi".equals(lang) && !"mr".equals(lang)) lang = "en"; // normalize
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));
        // After merging the pages, go straight to the questionnaire
        return "redirect:/questionnaire";
    }
}