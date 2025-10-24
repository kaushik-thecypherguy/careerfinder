package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

@Controller
public class LanguageController {

    @Autowired private UserService users;

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
        String email = (String) session.getAttribute("USER_EMAIL");
        String dbLang = null;
        if (email != null) {
            Optional<UserData> u = users.findByEmail(email);
            dbLang = u.map(UserData::getUiLang).orElse(null);
        }
        String current = (dbLang == null || dbLang.isBlank())
                ? (String) session.getAttribute("uiLang")
                : dbLang;
        if (current == null || current.isBlank()) current = "en";

        model.addAttribute("currentLang", current);
        model.addAttribute("langLocked", dbLang != null && !dbLang.isBlank()); // lock per-account, not per-session
        return "start";
    }

    /** Store chosen language per-account; then move to gating. */
    @PostMapping("/language/set")
    public String setLanguage(@RequestParam("lang") String lang, HttpSession session) {
        if (!"hi".equals(lang) && !"mr".equals(lang)) lang = "en";

        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null) return "redirect:/login";

        // Persist per-account, reflect in session, set Locale
        users.setUiLang(email, lang);
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));

        return "redirect:/gating";
    }
}