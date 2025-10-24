package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.QuestionnaireForm;
import com.acf.careerfinder.model.UserProgress.Section;
import com.acf.careerfinder.service.ProgressService;
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
    private final ProgressService progress;

    public QuestionnaireController(QuestionnaireService questionnaireService,
                                   QuestionBankService questionBankService,
                                   ProgressService progress) {
        this.questionnaireService = questionnaireService;
        this.questionBankService = questionBankService;
        this.progress = progress;
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
                                    @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {

        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        var g = progress.load(email, Section.GATING).orElse(null);
        if (g == null || !g.isCompleted()) return "redirect:/gating";

        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        session.setAttribute("uiLang", lang);
        LocaleContextHolder.setLocale(toLocale(lang));

        // Clamp pageSize to something reasonable.
        if (pageSize < 1) pageSize = 12;
        if (pageSize > 100) pageSize = 100;

        // Determine total pages once, then clamp requested page into range.
        var first = questionBankService.loadPage(lang, 1, pageSize);

        // If PageView is a record, use accessor:
        int totalPages = Math.max(1, first.totalPages());

        // If yours is a POJO with a getter, use this instead:
        // int totalPages = Math.max(1, first.getTotalPages());

        // If it's a public field, use:
        // int totalPages = Math.max(1, first.totalPages);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        // Load the requested (clamped) page
        var pv = questionBankService.loadPage(lang, page, pageSize);
        model.addAttribute("page", pv);

        // Prefill from DB + any in‑progress deltas
        LinkedHashMap<String,String> answers = new LinkedHashMap<>(questionnaireService.loadAnswersMap(email));
        progress.load(email, Section.QUESTIONNAIRE).ifPresent(up ->
                answers.putAll(progress.toMap(up.getAnswersJson()))
        );

        QuestionnaireForm form = new QuestionnaireForm();
        form.setAnswers(answers);
        model.addAttribute("form", form);

        // Remember last visited page for convenience
        progress.upsertMerge(email, Section.QUESTIONNAIRE, Map.of(), page);

        return "questionnaire_db";
    }

    /** Deep‑link for the “Edit your answers” button: jump straight to the last page. */
    @GetMapping("/questionnaire/last")
    public String goToLastQuestionnairePage(HttpSession session,
                                            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login";

        var g = progress.load(email, Section.GATING).orElse(null);
        if (g == null || !g.isCompleted()) return "redirect:/gating";

        String lang = (String) session.getAttribute("uiLang");
        if (lang == null || lang.isBlank()) lang = "en";
        LocaleContextHolder.setLocale(toLocale(lang));

        if (pageSize < 1) pageSize = 12;
        if (pageSize > 100) pageSize = 100;

        var first = questionBankService.loadPage(lang, 1, pageSize);

        // Record accessor (preferred if PageView is a record)
        int last = Math.max(1, first.totalPages());

        // If you have a getter or field, use one of these lines instead and remove the one above:
        // int last = Math.max(1, first.getTotalPages());
        // int last = Math.max(1, first.totalPages);

        return "redirect:/questionnaire?page=" + last + "&pageSize=" + pageSize;
    }

    /**
     * Final submit:
     *  - Merge previously saved answers + any in‑progress autosave deltas + the current page’s answers.
     *  - Save the merged map so NOTHING is lost even if JS is disabled.
     */
    @PostMapping("/questionnaire/submit")
    public String submit(@ModelAttribute("form") QuestionnaireForm form, HttpSession session) {
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) return "redirect:/login?error=loginRequired";

        LinkedHashMap<String,String> merged = new LinkedHashMap<>(questionnaireService.loadAnswersMap(email));
        progress.load(email, Section.QUESTIONNAIRE).ifPresent(up ->
                merged.putAll(progress.toMap(up.getAnswersJson()))
        );
        if (form != null && form.getAnswers() != null) {
            merged.putAll(form.getAnswers()); // latest page wins
        }

        questionnaireService.saveAnswers(email, merged);
        progress.markCompleted(email, Section.QUESTIONNAIRE); // mark done; progress rows won't interfere now

        return "redirect:/result";
    }
}