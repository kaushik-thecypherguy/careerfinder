package com.acf.careerfinder.service;

import com.acf.careerfinder.model.Recommendation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    public Recommendation compute(Map<String, String> a) {
        Map<String, String> answers = a == null ? Map.of() : a;

        String caste = answers.getOrDefault("Caste category", "Prefer not to say");
        String age   = answers.getOrDefault("Age group", "Unknown");
        String edu   = answers.getOrDefault("Highest education", "Unknown");
        String emp   = answers.getOrDefault("Employment status", "Unknown");
        String mode  = answers.getOrDefault("Preferred training mode", "On-site");

        List<String> careers = new ArrayList<>();
        List<String> steps   = new ArrayList<>();
        StringBuilder summary = new StringBuilder("Based on your education ('")
                .append(edu).append("'), employment status ('")
                .append(emp).append("') and preferred mode ('")
                .append(mode).append("'), we’ve shortlisted practical options.");

        // --- Basic rules by Education ---
        switch (edu) {
            case "Below 10th" -> {
                careers.addAll(List.of(
                        "Foundation skills (NSQF L2–3): Basic Electrical/Wiring Helper",
                        "Workshop Assistant / Fitter Assistant",
                        "Welding Helper / Fabrication Helper",
                        "Sewing Machine Operator / Tailoring Assistant",
                        "Basic Computer Operations (Typing, Office, Internet)"
                ));
                summary.append(" Start with foundation/bridge modules before core ITI trades.");
            }
            case "10th Pass" -> {
                careers.addAll(List.of(
                        "Electrician (ITI)",
                        "Fitter (ITI)",
                        "Welder (ITI)",
                        "COPA – Computer Operator & Programming Assistant (ITI)",
                        "Plumber / Carpenter / Mechanic (Diesel) (ITI)",
                        "Refrigeration & Air Conditioning Technician (ITI)"
                ));
                summary.append(" You’re eligible for core ITI trades and some short-term certifications.");
            }
            case "12th Pass" -> {
                careers.addAll(List.of(
                        "Electronics Mechanic (ITI)",
                        "COPA / DTP Operator (ITI)",
                        "Health Sanitary Inspector / Lab Assistant (paramedical entry routes)",
                        "Stenographer & Secretarial Assistant",
                        "Apprenticeship with local industry"
                ));
                summary.append(" You can take advanced trades and apprenticeships; consider sector skill council courses too.");
            }
            case "Diploma/ITI" -> {
                careers.addAll(List.of(
                        "CNC Operator / Machine Tool Technician",
                        "Solar PV Installer / Technician",
                        "Maintenance Technician (Electrical/Mechanical)",
                        "Mechatronics / Automation Assistant",
                        "QA/Production Assistant in MSMEs"
                ));
                summary.append(" You’re ready for advanced upskilling and specialized roles.");
            }
            case "Undergraduate", "Postgraduate" -> {
                careers.addAll(List.of(
                        "Junior Engineer / Supervisor (Apprentice)",
                        "CAD Technician (AutoCAD/SolidWorks basics)",
                        "Junior Data/Operations Assistant",
                        "Lab/Field Technician",
                        "Entrepreneurship in services (AC repair, solar installation, etc.)"
                ));
                summary.append(" Consider supervisory/apprentice roles and fast-track certifications.");
            }
            default -> {
                careers.addAll(List.of(
                        "Explore ITI core trades (Electrician, Fitter, Welder, COPA)",
                        "Bridge courses then Apprenticeship",
                        "Local MSME internships"
                ));
            }
        }

        // --- Nudge by Employment status ---
        switch (emp) {
            case "Unemployed" -> steps.add("Aim for courses with placement tie-ups or apprenticeship; start mock interviews.");
            case "Student"    -> steps.add("Pick flexible batches compatible with academics; target weekend/lite loads.");
            case "Full-time"  -> steps.add("Choose evening/online batches; negotiate study support at work.");
            case "Part-time"  -> steps.add("Balance shifts with hybrid courses; plan weekly practice schedule.");
            case "Self-employed" -> steps.add("Pick skills that extend your business (solar install, AC service, carpentry add-ons).");
        }

        // --- Mode preference tweaks ---
        if ("Online".equalsIgnoreCase(mode) || "Hybrid".equalsIgnoreCase(mode)) {
            careers.add(0, "Digital skills: MS Office, Spreadsheets, Email & Docs (online)");
            careers.add(1, "Basic Coding / No-code automation (online)");
            steps.add("Shortlist centers offering " + mode + " delivery; ensure there’s hands-on/practical days.");
        } else {
            steps.add("Shortlist nearby ITI/skill centers with strong practical labs (On-site).");
        }

        // --- Age hint for apprenticeship ---
        if (age.contains("18") || age.contains("22") || age.contains("25")) {
            steps.add("Consider National Apprenticeship routes (NAPS/NATS) — age window fits most programs.");
        }

        // --- Equity/benefits hint by category ---
        if (!"Prefer not to say".equalsIgnoreCase(caste)) {
            steps.add("Check category-based fee waivers/stipends and reserved seats (State & Central).");
        } else {
            steps.add("Explore general scholarships and fee support available in your state.");
        }

        // Deduplicate & keep order
        careers = new ArrayList<>(new LinkedHashSet<>(careers));
        steps   = new ArrayList<>(new LinkedHashSet<>(steps));

        String title = "Your Career Suggestions & Next Steps";

        return new Recommendation(
                title,
                summary.toString(),
                careers,
                steps,
                answers
        );
    }
}
