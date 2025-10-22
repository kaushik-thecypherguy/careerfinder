package com.acf.careerfinder.controller;

import com.acf.careerfinder.advice.SectorAdviceDTO;
import com.acf.careerfinder.sector.SectorConfigService;
import com.acf.careerfinder.service.ChatService;
import com.acf.careerfinder.service.QuestionnaireService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sector-advice")
public class SectorAdviceController {

    private final QuestionnaireService questionnaireService;
    private final SectorConfigService sectorConfig;
    private final ChatService chatService;
    private final ObjectMapper M = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    public SectorAdviceController(QuestionnaireService questionnaireService,
                                  SectorConfigService sectorConfig,
                                  ChatService chatService) {
        this.questionnaireService = questionnaireService;
        this.sectorConfig = sectorConfig;
        this.chatService = chatService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAdvice(@RequestParam("sectorId") String sectorId,
                                       @RequestParam(value = "detail", defaultValue = "full") String detail,
                                       HttpSession session) {
        // --- language & login guard ---
        String email = (String) session.getAttribute("USER_EMAIL");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "loginRequired"));
        }
        String lang = toLang((String) session.getAttribute("uiLang"));

        // --- answers we need ---
        Map<String,String> ans = questionnaireService.loadAnswersMap(email);
        String district = coalesce(ans.get("gate.Q25_district"), ans.get("gate.DISTRICT"));
        if (isBlank(district)) district = "Pune"; // safe default

        boolean hasDL = hasValidDL(ans.get("gate.Q12")); // C/D/E => true
        boolean has2W = "A".equalsIgnoreCase(norm(ans.get("gate.Q13"))); // Yes
        Integer ageYears = parseAge(ans.get("gate.Q26_ageYears"));

        // --- sector name from catalog or humanized id ---
        String sectorName = sectorNameFor(sectorId);

        // --- MH fallback hubs for district (simple guide) ---
        List<String> fallbackCities = fallbackForDistrict(district);

        // --- prompts ---
        String system = """
            You are a Maharashtra-focused jobs advisor for NSDC/Skill India pathways.
            Always answer in %s. Return ONLY a single JSON object using EXACT keys:

            sectorId, sectorName, language, userDistrict,
            cityFocus (array of cities in Maharashtra),
            whyFit (array of 2-3 short bullets),
            entryRoles (array of {title, notes}),
            startingSalaryINR { district, nearby:[{city, range}], note },
            whereToApply (array of {label, url} to official resources only),
            nearbyIfSparse { explanation, suggestedCities[] },
            gatingReminders (array),
            checklistWeek1 (array),
            disclaimers (array).

            Prioritize district '%s'. If roles there are thin, include nearby Maharashtra cities from the provided list.
            DO NOT filter or judge by the user's commute distance. It is OK to suggest cities that are farther away.
            Keep salaries typical for entry roles in Maharashtra and in INR per month.
            Use official sources only (NSDC/Skill India, Sector Skill Councils, Apprenticeship India, MahaSwayam).
            Do not add extra top-level keys.
            """.formatted(langName(lang), district);

        String user = """
            sectorId: %s
            sectorName: %s
            userDistrict: %s
            language: %s
            userHasDL: %s
            userHas2W: %s
            userAgeYears: %s
            detailLevel: %s   # "full" for Top-5, "brief" for near-miss (shorter lists)
            fallbackCities (MH only): %s

            Produce strictly-valid JSON with those exact keys. For "brief", keep only 1-2 roles and 2-3 whereToApply items.
            """.formatted(
                sectorId, sectorName, district, lang,
                hasDL, has2W, (ageYears == null ? "null" : ageYears.toString()),
                ("brief".equalsIgnoreCase(detail) ? "brief" : "full"),
                fallbackCities.toString()
        );

        // --- call LLM ---
        SectorAdviceDTO dto;
        try {
            String json = chatService.askJson(system, user);
            dto = M.readValue(json, SectorAdviceDTO.class);
        } catch (Exception ex) {
            dto = minimalFallback(lang, district, sectorId, sectorName, fallbackCities);
        }

        // --- patch defaults & enforce minimum ---
        dto.applyDefaults(lang, district, sectorId, sectorName, fallbackCities);

        return ResponseEntity.ok(dto);
    }

    /* ---------------- helpers ---------------- */

    private static String toLang(String s) {
        if (s == null) return "en";
        return switch (s) { case "hi" -> "hi"; case "mr" -> "mr"; default -> "en"; };
    }
    private static String langName(String lang) {
        return switch (lang) { case "hi" -> "Hindi"; case "mr" -> "Marathi"; default -> "English"; };
    }
    private static String norm(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
    private static boolean hasValidDL(String q12) {
        String v = norm(q12);
        return "C".equals(v) || "D".equals(v) || "E".equals(v);
    }
    private static Integer parseAge(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s.trim()); }
        catch (Exception ignore) { return null; }
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String coalesce(String a, String b) { return !isBlank(a) ? a : (!isBlank(b) ? b : null); }

    private String sectorNameFor(String id) {
        try {
            return sectorConfig.catalog().sectors().stream()
                    .filter(s -> Objects.equals(s.id(), id))
                    .findFirst()
                    .map(com.acf.careerfinder.sector.model1.SectorCatalog.Sector::name)
                    .orElse(humanize(id));
        } catch (Exception ignore) {
            return humanize(id);
        }
    }
    private static String humanize(String id) {
        if (id == null) return "Selected sector";
        return id.replace('_',' ').replaceAll("\\s+"," ").trim();
    }

    private static List<String> fallbackForDistrict(String dist) {
        String d = dist == null ? "" : dist.trim().toLowerCase(Locale.ROOT);
        Map<String,List<String>> map = Map.ofEntries(
                Map.entry("solapur", List.of("Solapur","Pune","Sangli","Kolhapur")),
                Map.entry("pune",    List.of("Pune","Mumbai","Nashik")),
                Map.entry("kolhapur",List.of("Kolhapur","Sangli","Pune")),
                Map.entry("sangli",  List.of("Sangli","Kolhapur","Pune")),
                Map.entry("mumbai",  List.of("Mumbai","Thane","Navi Mumbai")),
                Map.entry("thane",   List.of("Thane","Mumbai","Navi Mumbai")),
                Map.entry("nagpur",  List.of("Nagpur","Amravati","Wardha")),
                Map.entry("nashik",  List.of("Nashik","Mumbai","Pune"))
        );
        List<String> hubs = map.getOrDefault(d, List.of(cap(dist), "Pune", "Mumbai", "Nagpur", "Nashik"));
        // ensure district is first
        if (!hubs.isEmpty() && !hubs.get(0).equalsIgnoreCase(dist)) {
            List<String> copy = new ArrayList<>();
            copy.add(cap(dist));
            for (String c : hubs) if (!c.equalsIgnoreCase(dist)) copy.add(c);
            return copy;
        }
        return hubs;
    }
    private static String cap(String s) {
        if (s == null || s.isBlank()) return "Pune";
        return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static SectorAdviceDTO minimalFallback(String lang, String district,
                                                   String sectorId, String sectorName,
                                                   List<String> fallbackCities) {
        SectorAdviceDTO d = new SectorAdviceDTO();
        d.sectorId = sectorId;
        d.sectorName = sectorName;
        d.language = lang;
        d.userDistrict = district;
        d.cityFocus = new ArrayList<>(fallbackCities);
        d.whyFit = new ArrayList<>();
        d.entryRoles = new ArrayList<>();
        d.whereToApply = new ArrayList<>();
        // inject official links + defaults via applyDefaults(), but set note now
        SectorAdviceDTO.StartingSalary sal = new SectorAdviceDTO.StartingSalary();
        sal.note = switch (lang) {
            case "hi" -> "शिफ्ट/इंसेंटिव के आधार पर बदल सकता है";
            case "mr" -> "शिफ्ट/इन्सेंटिवनुसार बदलू शकते";
            default -> "Varies by shift and incentives";
        };
        d.startingSalaryINR = sal;
        d.nearbyIfSparse = new SectorAdviceDTO.NearbyIfSparse();
        d.nearbyIfSparse.explanation = switch (lang) {
            case "hi" -> "यदि ज़िले में अवसर कम हैं, तो पास के महाराष्ट्र शहर देखें।";
            case "mr" -> "जिल्ह्यात संधी कमी असतील तर जवळचे महाराष्ट्र शहर पहा.";
            default -> "If few roles in your district, consider nearby MH hubs.";
        };
        d.nearbyIfSparse.suggestedCities = new ArrayList<>(fallbackCities);
        d.gatingReminders = new ArrayList<>();
        d.checklistWeek1 = new ArrayList<>();
        d.disclaimers = new ArrayList<>();
        return d;
    }
}