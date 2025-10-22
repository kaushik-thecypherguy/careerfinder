package com.acf.careerfinder.sector;

import com.acf.careerfinder.geo.MHLocation;
import com.acf.careerfinder.sector.model1.SectorGates;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Age is taken ONLY from Q26 (gate.Q26_age). We do NOT infer age from the licence.
 * Min-age is enforced only if age is present AND below the requirement.
 */
@Service
public class EligibilityService {

    public static final class Cand {
        public Integer edu;
        public Integer age;           // Q26; may be null
        public Integer heightCm;
        public Integer liftKg;
        public Integer commuteKm;
        public Integer typingWPM;

        public Boolean standingOK;
        public Boolean nightOK;
        public Boolean weekendOK;
        public Boolean fieldTravelOK;
        public Boolean workAtHeightOK;

        public Boolean smartphone;
        public Boolean hasDocs;
        public Boolean hasDL;         // Q12 only (no age inference)
        public Boolean has2W;
        public Boolean hasPSARA;
        public Boolean hasAEP;
        public Boolean bgcOK;

        public Boolean englishBasic;
        public Boolean localLanguage;
        public Boolean computerBasics;
        public Boolean normalVision;
        public Boolean colorVisionOK;
        public Boolean vaccProof;
        public Boolean safetyInducted;

        public String stateCode;
        public String district;
    }

    public static final class Eligibility {
        public final boolean eligible;
        public final List<String> reasons;
        public Eligibility(boolean eligible, List<String> reasons) {
            this.eligible = eligible;
            this.reasons = (reasons == null) ? List.of() : List.copyOf(reasons);
        }
    }

    public Cand deriveCand(Map<String, String> gateAnswers) {
        if (gateAnswers == null) gateAnswers = Map.of();
        var cand = new Cand();

        cand.edu = mapChoiceIndex(gateAnswers.get("gate.Q1"), 7);

        {   // Q2 lift
            String v = upper(gateAnswers.get("gate.Q2"));
            if (v != null) {
                cand.liftKg = switch (v) {
                    case "A" -> 5;  case "B" -> 10; case "C" -> 15;
                    case "D" -> 20; case "E" -> 25; case "F" -> 30;
                    default -> null;
                };
            }
        }
        cand.standingOK = yesNo(gateAnswers.get("gate.Q3"));
        cand.nightOK    = yesNo(gateAnswers.get("gate.Q4"));
        cand.weekendOK  = yesNo(gateAnswers.get("gate.Q5"));

        {   // Q6 commute
            String v = upper(gateAnswers.get("gate.Q6"));
            if (v != null) {
                cand.commuteKm = switch (v) {
                    case "A" -> 5;  case "B" -> 10; case "C" -> 15;
                    case "D" -> 20; case "E" -> 30; case "F" -> 40;
                    default -> null;
                };
            }
        }
        cand.smartphone     = yesNo(gateAnswers.get("gate.Q7"));
        cand.computerBasics = basicOrAbove(gateAnswers.get("gate.Q8"));
        cand.englishBasic   = basicOrAbove(gateAnswers.get("gate.Q9"));
        cand.localLanguage  = basicOrAbove(gateAnswers.get("gate.Q10"));

        {   // Q11 docs
            String v = upper(gateAnswers.get("gate.Q11"));
            if (v != null) {
                cand.hasDocs = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q12 licence (valid only)
            String v = upper(gateAnswers.get("gate.Q12"));
            if (v != null) {
                cand.hasDL = switch (v) {
                    case "C", "D", "E" -> true;
                    case "A", "B" -> false;
                    default -> null;
                };
            }
        }
        cand.has2W = yesNo(gateAnswers.get("gate.Q13"));

        {   // Q14 PSARA
            String v = upper(gateAnswers.get("gate.Q14"));
            if (v != null) {
                cand.hasPSARA = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q15 AEP
            String v = upper(gateAnswers.get("gate.Q15"));
            if (v != null) {
                cand.hasAEP = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q16 BGC
            String v = upper(gateAnswers.get("gate.Q16"));
            if (v != null) {
                cand.bgcOK = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q17 normal vision
            String v = upper(gateAnswers.get("gate.Q17"));
            if (v != null) {
                cand.normalVision = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q18 colour
            String v = upper(gateAnswers.get("gate.Q18"));
            if (v != null) {
                cand.colorVisionOK = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }
        {   // Q19 height
            String v = upper(gateAnswers.get("gate.Q19"));
            if (v != null) {
                cand.heightCm = switch (v) {
                    case "A" -> 150; case "B" -> 155; case "C" -> 160;
                    case "D" -> 165; case "E" -> 170;
                    default -> null;
                };
            }
        }
        {   // Q20 vacc
            String v = upper(gateAnswers.get("gate.Q20"));
            if (v != null) {
                cand.vaccProof = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }
        cand.safetyInducted = yesNo(gateAnswers.get("gate.Q21"));
        cand.fieldTravelOK  = yesNo(gateAnswers.get("gate.Q22"));
        cand.workAtHeightOK = yesNo(gateAnswers.get("gate.Q23"));

        {   // Q24 typing
            String v = upper(gateAnswers.get("gate.Q24"));
            if (v != null) {
                cand.typingWPM = switch (v) {
                    case "A" -> 10; case "B" -> 20; case "C" -> 30;
                    case "D" -> 40; case "E" -> 55; case "F" -> 65;
                    default -> null;
                };
            }
        }

        // NEW: Q26 Age
        cand.age = parseAge(gateAnswers.get("gate.Q26_age"));

        // Location (MH, district canon)
        cand.stateCode = "MH";
        String distRaw = firstNonBlank(
                gateAnswers.get("gate.Q25_district"),
                gateAnswers.get("gate.DISTRICT")
        );
        cand.district  = MHLocation.canonicalize(distRaw);

        return cand;
    }

    public Eligibility checkEligibility(Cand cand, SectorGates.Requirements req) {
        if (req == null) return new Eligibility(true, List.of());
        List<String> reasons = new ArrayList<>();

        if (req.minEdu() != null && (cand.edu == null || cand.edu < req.minEdu()))
            reasons.add("Min education required: " + prettyEdu(req.minEdu()));

        // Age: fail only if provided AND below the requirement.
        if (req.minAge() != null && cand.age != null && cand.age < req.minAge())
            reasons.add("Min age required: " + req.minAge());

        if (req.minHeightCm() != null && (cand.heightCm == null || cand.heightCm < req.minHeightCm()))
            reasons.add("Min height required: " + req.minHeightCm() + " cm");

        if (req.minLiftKg() != null && (cand.liftKg == null || cand.liftKg < req.minLiftKg()))
            reasons.add("Must lift ≥ " + req.minLiftKg() + " kg");

        if (req.maxCommuteKm() != null && (cand.commuteKm == null || cand.commuteKm > req.maxCommuteKm()))
            reasons.add("Commute must be ≤ " + req.maxCommuteKm() + " km");

        need(req.needsStandingOk(),     cand.standingOK,     "Comfortable standing 6–8 hrs", reasons);
        need(req.needsNightOk(),        cand.nightOK,        "Night/rotational shifts OK",   reasons);
        need(req.needsWeekendOk(),      cand.weekendOK,      "Weekend work OK",              reasons);
        need(req.needsFieldTravelOk(),  cand.fieldTravelOK,  "Field travel OK",              reasons);
        need(req.needsWorkAtHeightOk(), cand.workAtHeightOK, "Work at height with safety gear OK", reasons);

        need(req.needsSmartphone(),     cand.smartphone,     "Smartphone for daily work",    reasons);
        need(req.needsDocs(),           cand.hasDocs,        "Standard ID & bank docs ready", reasons);
        need(req.needsDL(),             cand.hasDL,          "Valid driving licence",        reasons);
        need(req.needs2W(),             cand.has2W,          "Two‑wheeler access",           reasons);
        need(req.needsPSARA(),          cand.hasPSARA,       "PSARA certification",          reasons);
        need(req.needsAEP(),            cand.hasAEP,         "Airport Entry Permit (AEP)",   reasons);
        need(req.needsBGC(),            cand.bgcOK,          "Background check clearance",   reasons);

        need(req.needsEnglishBasic(),   cand.englishBasic,   "Basic English",                reasons);
        need(req.needsLocalLanguage(),  cand.localLanguage,  "Local language (basic+)",      reasons);
        need(req.needsComputerBasics(), cand.computerBasics, "Basic computer skills",        reasons);
        need(req.needsNormalVision(),   cand.normalVision,   "Normal/adequate vision",       reasons);
        need(req.needsColorVisionOk(),  cand.colorVisionOK,  "Normal colour vision",         reasons);
        need(req.needsVaccProof(),      cand.vaccProof,      "Vaccination proof",            reasons);

        if (req.minTypingWPM() != null && (cand.typingWPM == null || cand.typingWPM < req.minTypingWPM()))
            reasons.add("Typing ≥ " + req.minTypingWPM() + " WPM");

        return new Eligibility(reasons.isEmpty(), reasons);
    }

    private static void need(Boolean require, Boolean has, String label, List<String> reasons) {
        if (Boolean.TRUE.equals(require) && !Boolean.TRUE.equals(has)) reasons.add(label);
    }
    private static String upper(String s) { return (s == null) ? null : s.trim().toUpperCase(Locale.ROOT); }
    private static Boolean yesNo(String choice) {
        String c = upper(choice);
        if (c == null) return null;
        return switch (c) { case "A" -> true; case "B" -> false; default -> null; };
    }
    private static boolean basicOrAbove(String choice) {
        String c = upper(choice);
        if (c == null) return false;
        return switch (c) { case "A" -> false; case "B","C","D" -> true; default -> false; };
    }
    private static Integer mapChoiceIndex(String choice, int size) {
        String c = upper(choice);
        if (c == null || c.length() != 1) return null;
        int idx = c.charAt(0) - 'A';
        return (idx >= 0 && idx < size) ? idx : null;
    }
    private static Integer parseAge(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return (v < 14 || v > 70) ? null : v;
        } catch (NumberFormatException e) { return null; }
    }
    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
    private static String prettyEdu(int code) {
        return switch (code) {
            case 0 -> "No schooling"; case 1 -> "8th"; case 2 -> "10th"; case 3 -> "12th";
            case 4 -> "Diploma/ITI"; case 5 -> "Graduate"; case 6 -> "Post‑grad+";
            default -> String.valueOf(code);
        };
    }
}