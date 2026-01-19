package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Controller
public class LanguageController {

    @Autowired private UserService users;

    private static final Set<String> ALLOWED_LANGS = Set.of("en", "hi", "mr");

    private static String normalizeLang(String raw) {
        if (raw == null) return "en";
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_LANGS.contains(v) ? v : "en";
    }

    private static Locale toLocale(String lang) {
        return switch (lang) {
            case "hi" -> new Locale("hi");
            case "mr" -> new Locale("mr");
            default -> Locale.ENGLISH;
        };
    }

    /** Combined page: language select + intro video. Logged-in users only. */
    @GetMapping({"/start", "/language"})
    public String showStart(HttpSession session, Model model) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        Optional<UserData> u = users.findByEmail(email);
        String dbLang = u.map(UserData::getUiLang).orElse(null);

        String current = (dbLang != null && !dbLang.isBlank())
                ? normalizeLang(dbLang)
                : normalizeLang((String) session.getAttribute("uiLang"));

        boolean locked = (dbLang != null && !dbLang.isBlank());

        session.setAttribute("uiLang", current);
        LocaleContextHolder.setLocale(toLocale(current));

        model.addAttribute("currentLang", current);
        model.addAttribute("langLocked", locked);

        return "start";
    }

    /** Store chosen language per-account; then move to gating. */
    @PostMapping("/language/set")
    public String setLanguage(@RequestParam("lang") String lang, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        // Lock enforcement on server, not only UI.
        String existing = users.getUiLang(email).orElse(null);
        if (existing != null && !existing.isBlank()) {
            String fixed = normalizeLang(existing);
            session.setAttribute("uiLang", fixed);
            LocaleContextHolder.setLocale(toLocale(fixed));
            return "redirect:/gating";
        }

        String chosen = normalizeLang(lang);
        users.setUiLang(email, chosen);

        session.setAttribute("uiLang", chosen);
        LocaleContextHolder.setLocale(toLocale(chosen));

        return "redirect:/gating";
    }
}