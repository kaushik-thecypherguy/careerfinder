package com.acf.careerfinder.controller;

import com.acf.careerfinder.model.UserProgress;
import com.acf.careerfinder.model.UserProgress.Section;
import com.acf.careerfinder.service.ProgressService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressApiController {

    @Autowired private ProgressService progress;

    private String requireEmail(HttpSession session) {
        Object v = session.getAttribute("USER_EMAIL");
        return v == null ? null : v.toString();
    }

    /** Load merged progress for both sections (for hydration on page load). */
    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session) {
        String email = requireEmail(session);
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in.");

        var gating = progress.load(email, Section.GATING).orElse(null);
        var qn     = progress.load(email, Section.QUESTIONNAIRE).orElse(null);

        return ResponseEntity.ok(Map.of(
                "gating", Map.of(
                        "answers", gating == null ? Map.of() : progress.toMap(gating.getAnswersJson()),
                        "page",    gating == null ? 1 : gating.getPage()
                ),
                "questionnaire", Map.of(
                        "answers", qn == null ? Map.of() : progress.toMap(qn.getAnswersJson()),
                        "page",    qn == null ? 1 : qn.getPage()
                )
        ));
    }

    /** Auto‑save deltas for a section. Body: { section, answers:{...}, page } */
    @PostMapping
    public ResponseEntity<?> saveDelta(@RequestBody Map<String,Object> body, HttpSession session) {
        String email = requireEmail(session);
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in.");

        String sectionStr = String.valueOf(body.getOrDefault("section", "")).trim().toUpperCase();
        Section section;
        try {
            section = Section.valueOf(sectionStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or missing 'section' (GATING | QUESTIONNAIRE).");
        }

        @SuppressWarnings("unchecked")
        Map<String,String> answers = (Map<String,String>) body.get("answers");

        Integer page = null;
        if (body.containsKey("page")) {
            try { page = Integer.valueOf(String.valueOf(body.get("page"))); }
            catch (Exception ignore) { /* leave null */ }
        }

        var saved = progress.upsertMerge(email, section, answers, page);
        return ResponseEntity.ok(Map.of(
                "status","ok",
                "section", section,
                "page", saved.getPage(),
                "savedAnswers", progress.toMap(saved.getAnswersJson())
        ));
    }
}