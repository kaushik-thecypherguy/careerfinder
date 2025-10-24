package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.UserProgress.Section;
import com.acf.careerfinder.service.ProgressService;
import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResumeController {

    @Autowired private UserService users;
    @Autowired private ProgressService progress;

    @GetMapping("/resume")
    public String resume(HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        // 1) Language per-account
        String lang = users.getUiLang(email).orElse(null);
        if (lang == null || lang.isBlank()) {
            session.removeAttribute("uiLang");
            return "redirect:/start"; // pick language
        }
        session.setAttribute("uiLang", lang);

        // 2) Gating first
        var g = progress.load(email, Section.GATING).orElse(null);
        if (g == null || !g.isCompleted()) return "redirect:/gating";

        // 3) Questionnaire next (last page to resume)
        var q = progress.load(email, Section.QUESTIONNAIRE).orElse(null);
        if (q == null || !q.isCompleted()) {
            int p = (q != null && q.getPage() >= 1) ? q.getPage() : 1;
            return "redirect:/questionnaire?page=" + p;
        }

        // 4) All done → results
        return "redirect:/result";
    }
}