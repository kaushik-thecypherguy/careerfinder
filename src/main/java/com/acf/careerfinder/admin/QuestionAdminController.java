package com.acf.careerfinder.admin;

import com.acf.careerfinder.model.QItem;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/questions")
public class QuestionAdminController {

    private final QuestionAdminService service;
    private final AdminGuard guard;

    public QuestionAdminController(QuestionAdminService service, AdminGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @GetMapping
    public String page(@RequestParam(value = "ok", required = false) String ok,
                       @RequestParam(value = "msg", required = false) String msg,
                       Model model) {
        model.addAttribute("ok", ok);
        model.addAttribute("msg", msg);
        return "admin_questions";
    }

    @PostMapping(path = "/upsert", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String upsert(@ModelAttribute AdminQuestionDTO dto,
                         @RequestHeader(value = "X-Admin-Secret", required = false) String hdrSecret,
                         @RequestParam(value = "secret", required = false) String paramSecret) {
        guard.check(first(hdrSecret, paramSecret));
        service.upsert(dto);
        return "redirect:/admin/questions?ok=upsert&msg=" + url("Saved " + dto.getQkey());
    }

    @PostMapping(path = "/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String delete(@RequestParam("qkey") String qkey,
                         @RequestHeader(value = "X-Admin-Secret", required = false) String hdrSecret,
                         @RequestParam(value = "secret", required = false) String paramSecret) {
        guard.check(first(hdrSecret, paramSecret));
        service.deleteByQkey(qkey);
        return "redirect:/admin/questions?ok=delete&msg=" + url("Deleted " + qkey);
    }

    @GetMapping(path = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AdminQuestionDTO load(@RequestParam("qkey") String qkey,
                                 @RequestHeader(value = "X-Admin-Secret", required = false) String hdrSecret,
                                 @RequestParam(value = "secret", required = false) String paramSecret) {
        guard.check(first(hdrSecret, paramSecret));
        Optional<QItem> row = service.findByQkey(qkey);
        return row.map(service::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + qkey));
    }

    private static String first(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }
    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @GetMapping("/list")
    public String list(@RequestParam(value = "lang", required = false) String lang,
                       HttpSession session,
                       Model model) {
        String L = (lang != null && !lang.isBlank())
                ? lang
                : (String) session.getAttribute("ADMIN_LANG");
        if (L == null) L = "en";
        session.setAttribute("ADMIN_LANG", L);

        model.addAttribute("currentLang", L);
        model.addAttribute("rows", service.listRows(L));
        return "admin_questions_list";
    }
}