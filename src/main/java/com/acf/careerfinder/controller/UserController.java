package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.model.Recommendation;
import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.service.QuestionnaireService;
import com.acf.careerfinder.service.RecommendationService;
import com.acf.careerfinder.service.UserService;
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

    @GetMapping("/questionnaire")
    public String questionnaire(Model model, HttpSession session, RedirectAttributes ra) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            ra.addAttribute("redirect", "questionnaire");
            return "redirect:/login";
        }
        Map<String, String> saved = questionnaireService.loadAnswersMap(email);
        QuestionnaireForm form = new QuestionnaireForm();
        form.setAnswers(saved);
        model.addAttribute("form", form);
        return "questionnaire";
    }

    // --------- Actions (POST) ----------
    @PostMapping("/CreateUser")
    public String createUser(@ModelAttribute("user") UserData u, RedirectAttributes ra) {
        try {
            service.createUser(u);
            ra.addAttribute("registered", "");
            ra.addAttribute("email", u.getEmail());
            return "redirect:/login";
        } catch (UserService.EmailAlreadyExistsException ex) {
            ra.addAttribute("conflict", "");
            ra.addAttribute("email", u.getEmail());
            return "redirect:/add-user";
        }
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
        return "redirect:/questionnaire";
    }

    // --------- GET fallbacks to avoid 405s ----------
    @GetMapping("/CreateUser")
    public String createUserGetFallback() { return "redirect:/add-user"; }

    @GetMapping("/CheckLogin")
    public String checkLoginGetFallback() { return "redirect:/login"; }

    // --------- Questionnaire submit -> RESULT ----------
    @PostMapping("/questionnaire/submit")
    public String submitQuestionnaire(@ModelAttribute("form") QuestionnaireForm form,
                                      HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        Map<String, String> answers = form.getAnswers();
        questionnaireService.saveAnswers(email, answers);
        return "redirect:/result";
    }

    @GetMapping("/questionnaire/submit")
    public String questionnaireSubmitGetFallback() { return "redirect:/questionnaire"; }

    // --------- Result page ----------
    @GetMapping("/result")
    public String resultPage(Model model, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        Map<String,String> answers = questionnaireService.loadAnswersMap(email);
        Recommendation rec = recommendationService.compute(answers);
        model.addAttribute("rec", rec);
        return "result"; // templates/result.html
    }

    // --------- Logout (allow both) ----------
    @PostMapping("/logout")
    public String doLogoutPost(HttpSession session, RedirectAttributes ra) {
        session.invalidate(); ra.addAttribute("logout", ""); return "redirect:/login";
    }
    @GetMapping("/logout")
    public String doLogoutGet(HttpSession session, RedirectAttributes ra) {
        session.invalidate(); ra.addAttribute("logout", ""); return "redirect:/login";
    }
}
