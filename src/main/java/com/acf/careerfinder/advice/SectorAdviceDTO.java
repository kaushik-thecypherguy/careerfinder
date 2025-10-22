package com.acf.careerfinder.advice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SectorAdviceDTO {

    public String sectorId;
    public String sectorName;
    public String language;        // "en" | "hi" | "mr"
    public String userDistrict;

    public List<String> cityFocus;         // e.g., ["Solapur","Pune"]
    public List<String> whyFit;            // 2–3 bullets
    public List<EntryRole> entryRoles;     // optional, hide if empty
    public StartingSalary startingSalaryINR;
    public List<Link> whereToApply;        // inject official links if empty
    public NearbyIfSparse nearbyIfSparse;  // optional
    public List<String> gatingReminders;   // short bullets (age 18+, DL/2W etc.)
    public List<String> checklistWeek1;    // 3 bullets
    public List<String> disclaimers;       // 1–2 bullets

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EntryRole {
        public String title;
        public String notes;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StartingSalary {
        public String district; // "₹12k–₹18k / month"
        public List<NearbySalary> nearby; // [{city, range}]
        public String note;     // "Varies by shift..."
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NearbySalary {
        public String city;
        public String range;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link {
        public String label;
        public String url;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NearbyIfSparse {
        public String explanation;
        public List<String> suggestedCities;
    }

    /* -------------- convenience: patch defaults & ensure minimum -------------- */

    public void applyDefaults(String lang, String district,
                              String sectorId, String sectorName,
                              List<String> fallbackCities) {
        if (isBlank(this.language)) this.language = lang;
        if (isBlank(this.userDistrict)) this.userDistrict = district;
        if (isBlank(this.sectorId)) this.sectorId = sectorId;
        if (isBlank(this.sectorName)) this.sectorName = sectorName;

        if (this.cityFocus == null || this.cityFocus.isEmpty()) {
            this.cityFocus = new ArrayList<>(fallbackCities);
        }

        // Keep whyFit/entryRoles optional (UI hides if empty)

        // Ensure whereToApply has at least official first steps
        if (this.whereToApply == null) this.whereToApply = new ArrayList<>();
        if (this.whereToApply.isEmpty()) {
            addOfficialLinks(this.whereToApply, lang, district);
        }

        // Starting salary: make note present so card doesn't look broken
        if (this.startingSalaryINR == null) this.startingSalaryINR = new StartingSalary();
        if (isBlank(this.startingSalaryINR.note)) {
            this.startingSalaryINR.note = noteByLang(lang);
        }

        // Minimal week‑1 checklist if missing
        if (this.checklistWeek1 == null || this.checklistWeek1.isEmpty()) {
            this.checklistWeek1 = defaultChecklist(lang);
        }

        // Disclaimers if missing
        if (this.disclaimers == null || this.disclaimers.isEmpty()) {
            this.disclaimers = defaultDisclaimers(lang);
        }
    }

    private static void addOfficialLinks(List<Link> out, String lang, String district) {
        out.add(link(labelNSDC(lang), "https://www.nsdcindia.org/"));
        out.add(link(labelNAPS(lang), "https://www.apprenticeshipindia.gov.in/"));
        out.add(link(labelMaha(lang, district), "https://rojgar.mahaswayam.gov.in/"));
    }

    private static Link link(String label, String url) {
        Link l = new Link();
        l.label = label; l.url = url; return l;
    }

    private static String labelNSDC(String lang) {
        return switch (lang) {
            case "hi" -> "Skill India / NSDC – सेक्टर काउंसिल";
            case "mr" -> "Skill India / NSDC – सेक्टर काउन्सिल";
            default -> "Skill India / NSDC – Sector Council";
        };
    }
    private static String labelNAPS(String lang) {
        return switch (lang) {
            case "hi" -> "Apprenticeship India (NAPS) – संबंधित भूमिकाएँ";
            case "mr" -> "Apprenticeship India (NAPS) – संबंधित भूमिका";
            default -> "Apprenticeship India (NAPS) – relevant roles";
        };
    }
    private static String labelMaha(String lang, String district) {
        return switch (lang) {
            case "hi" -> "MahaSwayam – " + district + " जिला";
            case "mr" -> "MahaSwayam – " + district + " जिल्हा";
            default -> "MahaSwayam – " + district + " district";
        };
    }

    private static List<String> defaultChecklist(String lang) {
        List<String> l = new ArrayList<>();
        switch (lang) {
            case "hi" -> {
                l.add("Skill India पर अपने सेक्टर का 1 छोटा मॉड्यूल पूरा करें");
                l.add("Resume अपडेट करें (एंट्री‑लेवल कीवर्ड)");
                l.add("MahaSwayam + NAPS पर 5–7 आवेदन करें");
            }
            case "mr" -> {
                l.add("Skill India वर आपल्या सेक्टरचा छोटा मॉड्यूल पूर्ण करा");
                l.add("रिझ्युमे अपडेट करा (एंट्री‑लेवल कीवर्ड)");
                l.add("MahaSwayam + NAPS वर 5–7 अर्ज करा");
            }
            default -> {
                l.add("Finish a short Skill India module for your sector");
                l.add("Update resume (entry‑level keywords)");
                l.add("Apply to 5–7 roles on MahaSwayam + NAPS");
            }
        }
        return l;
    }

    private static List<String> defaultDisclaimers(String lang) {
        List<String> l = new ArrayList<>();
        switch (lang) {
            case "hi" -> l.add("वेतन/खाली पद बदलते रहते हैं; आधिकारिक लिस्टिंग देखें।");
            case "mr" -> l.add("पगार/खुली पदे बदलू शकतात; अधिकृत लिस्टिंग पाहा.");
            default -> l.add("Salaries/openings change; check the official listing.");
        }
        return l;
    }

    private static String noteByLang(String lang) {
        return switch (lang) {
            case "hi" -> "शिफ्ट, इंसेंटिव, ESI/EPF के आधार पर बदल सकता है";
            case "mr" -> "शिफ्ट, इन्सेंटिव, ESI/EPF नुसार बदलू शकते";
            default -> "Varies by shift, incentives, ESI/EPF";
        };
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /* Light completeness signal (optional for logging) */
    public boolean hasMinimum() {
        return sectorId != null && language != null && userDistrict != null && whereToApply != null && !whereToApply.isEmpty();
    }
}