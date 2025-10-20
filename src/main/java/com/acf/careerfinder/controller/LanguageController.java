package com.acf.careerfinder.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    /** Combined page: language select + intro video. */
    @GetMapping({"/start", "/language"})
    public String showStart(HttpSession session, Model model) {
        String current = (String) session.getAttribute("uiLang");
        if (current == null || current.isBlank()) current = "en";
        model.addAttribute("currentLang", current);
        model.addAttribute("langLocked", Boolean.TRUE.equals(session.getAttribute("LANG_LOCKED")));
        return "start";
    }

    /** Store chosen language unless already locked; then go to gating. */
    @PostMapping("/language/set")
    public String setLanguage(@RequestParam("lang") String lang, HttpSession session) {
        if (!"hi".equals(lang) && !"mr".equals(lang)) lang = "en";
        if (Boolean.TRUE.equals(session.getAttribute("LANG_LOCKED"))) {
            return "redirect:/questionnaire?info=languageLocked";
        }
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));
        return "redirect:/gating";
    }
}