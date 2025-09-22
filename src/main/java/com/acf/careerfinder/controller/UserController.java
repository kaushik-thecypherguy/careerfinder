package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.model.Recommendation;
import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.service.QuestionnaireService;
import com.acf.careerfinder.service.RecommendationService;
import com.acf.careerfinder.service.UserService;
import com.acf.careerfinder.service.PasswordValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired private UserService service;
    @Autowired private QuestionnaireService questionnaireService;
    @Autowired private RecommendationService recommendationService;
    @Autowired private PasswordValidator passwordValidator;

    // --------- Pages ----------
    @GetMapping("/")
    public String home() { return "redirect:/questionnaire"; }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new UserData());
        return "add-user";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    // --------- Actions (POST) ----------
    @PostMapping("/CreateUser")
    public String createUser(@ModelAttribute("user") UserData u,
                             HttpSession session,
                             RedirectAttributes ra) {
        try {
            // Enforce server-side password policy
            passwordValidator.validateOrThrow(u.getUserpassword());

            service.createUser(u);

            // Auto-login the user and send to language select
            session.setAttribute("USER_EMAIL", u.getEmail());
            ra.addAttribute("registered", "");
            return "redirect:/language";
        } catch (IllegalArgumentException weakPw) {
            // Weak password → return to signup with banner and preserve fields
            ra.addAttribute("weakpw", "");
            ra.addAttribute("email", u.getEmail());
            ra.addAttribute("username", u.getUsername());
            return "redirect:/add-user";
        } catch (UserService.EmailAlreadyExistsException ex) {
            ra.addAttribute("conflict", "");
            ra.addAttribute("email", u.getEmail());
            return "redirect:/add-user";
        }
    }

    // REPLACEMENT (does NOT collide)
    @GetMapping("/account")
    public String accountHome() {
        return "redirect:/questionnaire";
    }

    @PostMapping("/CheckLogin")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes ra) {
        Optional<UserData> maybeUser = service.findByEmail(email);
        if (maybeUser.isEmpty()) {
            ra.addAttribute("error", "unknown");
            ra.addAttribute("email", email);
            return "redirect:/login";
        }
        UserData user = maybeUser.get();
        if (!user.isEnabled()) {
            ra.addAttribute("error", "disabled");
            ra.addAttribute("email", email);
            return "redirect:/login";
        }
        if (user.getUserpassword() == null || !user.getUserpassword().equals(password)) {
            ra.addAttribute("error", "badpw");
            ra.addAttribute("email", email);
            return "redirect:/login";
        }
        session.setAttribute("USER_EMAIL", email);
        return "redirect:/language";  // go through language → video → questionnaire
    }

    // --------- GET fallbacks to avoid 405s ----------
    @GetMapping("/CreateUser")
    public String createUserGetFallback() { return "redirect:/add-user"; }

    @GetMapping("/CheckLogin")
    public String checkLoginGetFallback() { return "redirect:/login"; }

    // --------- Logout (allow both) ----------
    @PostMapping("/logout")
    public String doLogoutPost(HttpSession session, RedirectAttributes ra) {
        session.invalidate(); ra.addAttribute("logout", ""); return "redirect:/login";
    }
}
