package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.service.PasswordValidator;
import com.acf.careerfinder.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired private UserService service;
    @Autowired private PasswordValidator passwordValidator;

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (session == null) ? null : (String) session.getAttribute("USER_EMAIL");

        if (email == null || email.isBlank()) {
            return "sample-questions"; // templates/sample-questions.html
        }
        return "redirect:/resume";
    }

    @GetMapping("/sample")
    public String sampleHome() {
        return "redirect:/";
    }

    @GetMapping("/sample/questions")
    public String sampleQuestionnaire(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (session == null) ? null : (String) session.getAttribute("USER_EMAIL");
        if (email != null && !email.isBlank()) return "redirect:/resume";
        return "sample-questionnaire";
    }

    @GetMapping("/sample/result")
    public String sampleResult(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (session == null) ? null : (String) session.getAttribute("USER_EMAIL");
        if (email != null && !email.isBlank()) return "redirect:/resume";
        return "sample-result";
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new UserData());
        return "add-user";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/CreateUser")
    public String createUser(@ModelAttribute("user") UserData u,
                             HttpSession session,
                             RedirectAttributes ra) {
        try {
            passwordValidator.validateOrThrow(u.getUserpassword());
            service.createUser(u);

            session.setAttribute("USER_EMAIL", u.getEmail());
            return "redirect:/resume";
        } catch (IllegalArgumentException weakPw) {
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

    @GetMapping("/account")
    public String accountHome() { return "redirect:/resume"; }

    @PostMapping("/CheckLogin")
    public String login(@RequestParam(value = "email", required = false) String emailParam,
                        @RequestParam(value = "identifier", required = false) String identifier,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes ra) {

        String loginKey = (emailParam != null && !emailParam.isBlank()) ? emailParam : identifier;

        Optional<UserData> maybeUser = (loginKey != null && loginKey.contains("@"))
                ? service.findByEmail(loginKey)
                : service.findByUsername(loginKey);

        if (maybeUser.isEmpty()) {
            ra.addAttribute("error", "unknown");
            ra.addAttribute("email", loginKey);
            return "redirect:/login";
        }

        UserData user = maybeUser.get();
        if (!user.isEnabled()) {
            ra.addAttribute("error", "disabled");
            ra.addAttribute("email", loginKey);
            return "redirect:/login";
        }
        if (user.getUserpassword() == null || !user.getUserpassword().equals(password)) {
            ra.addAttribute("error", "badpw");
            ra.addAttribute("email", loginKey);
            return "redirect:/login";
        }

        session.setAttribute("USER_EMAIL", user.getEmail());
        return "redirect:/resume";
    }

    @GetMapping("/CreateUser")
    public String createUserGetFallback() { return "redirect:/add-user"; }

    @GetMapping("/CheckLogin")
    public String checkLoginGetFallback() { return "redirect:/login"; }

    @PostMapping("/logout")
    public String doLogoutPost(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addAttribute("logout", "");
        return "redirect:/login";
    }

    @GetMapping("/account/login-info")
    @ResponseBody
    public ResponseEntity<?> loginInfo(HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        UserData user = service.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Session stale"));
        }
        boolean show = (user.getLoginIdShownAt() == null);
        return ResponseEntity.ok(Map.of(
                "show", show,
                "loginId", email,
                "password", show ? user.getUserpassword() : null
        ));
    }

    @PostMapping("/account/login-info/ack")
    @ResponseBody
    public ResponseEntity<?> markLoginIdShown(HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        service.markLoginIdShown(email);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}