package com.acf.careerfinder.sector;

import com.acf.careerfinder.sector.model1.SectorGates;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Phase-7:
 *  (a) derive normalized candidate gating attributes (Cand_*) from gate.Q1..Q24,
 *  (b) evaluate eligibility vs sector gate requirements and return reasons if ineligible.
 *
 * Notes:
 * - Mapping follows your gating doc (Q1..Q24 -> Cand_*).
 * - Any null field in requirements is treated as "not required".
 */
@Service
public class EligibilityService {

    /** Normalized gating attributes derived from gate.* answers. */
    public static final class Cand {
        // numeric
        public Integer edu;          // 0..6 (Q1)
        public Integer age;          // years (not asked yet; keep null)
        public Integer heightCm;     // Q19 lower-bound mapping
        public Integer liftKg;       // Q2 lower-bound mapping
        public Integer commuteKm;    // Q6 upper-bound mapping
        public Integer typingWPM;    // Q24 -> 10/20/30/40/55/65

        // booleans
        public Boolean standingOK;       // Q3
        public Boolean nightOK;          // Q4
        public Boolean weekendOK;        // Q5
        public Boolean fieldTravelOK;    // Q22
        public Boolean workAtHeightOK;   // Q23

        public Boolean smartphone;       // Q7
        public Boolean hasDocs;          // Q11
        public Boolean hasDL;            // Q12
        public Boolean has2W;            // Q13
        public Boolean hasPSARA;         // Q14
        public Boolean hasAEP;           // Q15
        public Boolean bgcOK;            // Q16

        public Boolean englishBasic;     // Q9 (Basic+)
        public Boolean localLanguage;    // Q10 (Basic+)
        public Boolean computerBasics;   // Q8 (Basic+)
        public Boolean normalVision;     // Q17
        public Boolean colorVisionOK;    // Q18
        public Boolean vaccProof;        // Q20
        public Boolean safetyInducted;   // Q21
    }

    /** Result of a single sector eligibility evaluation. */
    public static final class Eligibility {
        public final boolean eligible;
        public final List<String> reasons; // empty when eligible

        public Eligibility(boolean eligible, List<String> reasons) {
            this.eligible = eligible;
            this.reasons = (reasons == null) ? List.of() : List.copyOf(reasons);
        }
    }

    /** Core: derive Cand_* from raw gate.Q1..Q24 answers.  (NPE-safe) */
    public Cand deriveCand(Map<String, String> gateAnswers) {
        if (gateAnswers == null) gateAnswers = Map.of();
        var cand = new Cand();

        // Q1 Education: A..G -> 0..6
        cand.edu = mapChoiceIndex(gateAnswers.get("gate.Q1"), 7); // 0..6

        // Q2 Lift (lower bound: 5/10/15/20/25/30)
        {
            String v = upper(gateAnswers.get("gate.Q2"));
            if (v != null) {
                cand.liftKg = switch (v) {
                    case "A" -> 5;  case "B" -> 10; case "C" -> 15;
                    case "D" -> 20; case "E" -> 25; case "F" -> 30;
                    default -> null;
                };
            }
        }

        // Q3 Standing long hours
        cand.standingOK = yesNo(gateAnswers.get("gate.Q3"));

        // Q4 Night shifts
        cand.nightOK = yesNo(gateAnswers.get("gate.Q4"));

        // Q5 Weekend
        cand.weekendOK = yesNo(gateAnswers.get("gate.Q5"));

        // Q6 Commute (upper bound: 5/10/15/20/30/40)
        {
            String v = upper(gateAnswers.get("gate.Q6"));
            if (v != null) {
                cand.commuteKm = switch (v) {
                    case "A" -> 5;  case "B" -> 10; case "C" -> 15;
                    case "D" -> 20; case "E" -> 30; case "F" -> 40;
                    default -> null;
                };
            }
        }

        // Q7 Smartphone
        cand.smartphone = yesNo(gateAnswers.get("gate.Q7"));

        // Q8 Computer basics: Basic or above = true
        cand.computerBasics = basicOrAbove(gateAnswers.get("gate.Q8"));

        // Q9 English: Basic+ true
        cand.englishBasic = basicOrAbove(gateAnswers.get("gate.Q9"));

        // Q10 Local language: Basic+ true
        cand.localLanguage = basicOrAbove(gateAnswers.get("gate.Q10"));

        // Q11 Docs: Yes/Partly -> true
        {
            String v = upper(gateAnswers.get("gate.Q11"));
            if (v != null) {
                cand.hasDocs = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }

        // Q12 DL: only valid licences => true
        {
            String v = upper(gateAnswers.get("gate.Q12"));
            if (v != null) {
                cand.hasDL = switch (v) {
                    case "C", "D", "E" -> true;      // 2W / LMV / HMV
                    case "A", "B" -> false;          // No / Learner
                    default -> null;
                };
            }
        }

        // Q13 Two-wheeler access
        cand.has2W = yesNo(gateAnswers.get("gate.Q13"));

        // Q14 PSARA: Have / Training-pending -> true
        {
            String v = upper(gateAnswers.get("gate.Q14"));
            if (v != null) {
                cand.hasPSARA = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }

        // Q15 AEP: only A => true
        {
            String v = upper(gateAnswers.get("gate.Q15"));
            if (v != null) {
                cand.hasAEP = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }

        // Q16 Background check
        {
            String v = upper(gateAnswers.get("gate.Q16"));
            if (v != null) {
                cand.bgcOK = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }

        // Q17 Vision
        {
            String v = upper(gateAnswers.get("gate.Q17"));
            if (v != null) {
                cand.normalVision = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }

        // Q18 Colour vision
        {
            String v = upper(gateAnswers.get("gate.Q18"));
            if (v != null) {
                cand.colorVisionOK = switch (v) {
                    case "A" -> true;
                    case "B", "C" -> false;
                    default -> null;
                };
            }
        }

        // Q19 Height (lower bound 150/155/160/165/170)
        {
            String v = upper(gateAnswers.get("gate.Q19"));
            if (v != null) {
                cand.heightCm = switch (v) {
                    case "A" -> 150; case "B" -> 155; case "C" -> 160;
                    case "D" -> 165; case "E" -> 170;
                    default -> null;
                };
            }
        }

        // Q20 Vaccination proof: A/B -> true
        {
            String v = upper(gateAnswers.get("gate.Q20"));
            if (v != null) {
                cand.vaccProof = switch (v) {
                    case "A", "B" -> true;
                    case "C" -> false;
                    default -> null;
                };
            }
        }

        // Q21 Safety induction
        cand.safetyInducted = yesNo(gateAnswers.get("gate.Q21"));

        // Q22 Field travel
        cand.fieldTravelOK = yesNo(gateAnswers.get("gate.Q22"));

        // Q23 Work at height
        cand.workAtHeightOK = yesNo(gateAnswers.get("gate.Q23"));

        // Q24 Typing WPM: 10/20/30/40/55/65
        {
            String v = upper(gateAnswers.get("gate.Q24"));
            if (v != null) {
                cand.typingWPM = switch (v) {
                    case "A" -> 10; case "B" -> 20; case "C" -> 30;
                    case "D" -> 40; case "E" -> 55; case "F" -> 65;
                    default -> null;
                };
            }
        }

        // Age: not captured yet → cand.age remains null.
        return cand;
    }

    /** Compare candidate vs sector requirements; null requirement fields are ignored. */
    public Eligibility checkEligibility(Cand cand, SectorGates.Requirements req) {
        if (req == null) return new Eligibility(true, List.of());

        List<String> reasons = new ArrayList<>();

        // numeric checks
        if (req.minEdu() != null && (cand.edu == null || cand.edu < req.minEdu()))
            reasons.add("Min education required: " + prettyEdu(req.minEdu()));

        if (req.minAge() != null && (cand.age == null || cand.age < req.minAge()))
            reasons.add("Min age required: " + req.minAge());

        if (req.minHeightCm() != null && (cand.heightCm == null || cand.heightCm < req.minHeightCm()))
            reasons.add("Min height required: " + req.minHeightCm() + " cm");

        if (req.minLiftKg() != null && (cand.liftKg == null || cand.liftKg < req.minLiftKg()))
            reasons.add("Must lift ≥ " + req.minLiftKg() + " kg");

        if (req.maxCommuteKm() != null && (cand.commuteKm == null || cand.commuteKm > req.maxCommuteKm()))
            reasons.add("Commute must be ≤ " + req.maxCommuteKm() + " km");

        // boolean checks (true => must have)
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

    /* ---------- helpers ---------- */

    private static void need(Boolean require, Boolean has, String label, List<String> reasons) {
        if (Boolean.TRUE.equals(require) && !Boolean.TRUE.equals(has)) {
            reasons.add(label);
        }
    }

    private static String upper(String s) { return (s == null) ? null : s.trim().toUpperCase(Locale.ROOT); }

    private static Boolean yesNo(String choice) {
        String c = upper(choice);
        if (c == null) return null;
        return switch (c) {
            case "A" -> true;   // Yes
            case "B" -> false;  // No
            default -> null;
        };
    }

    private static boolean basicOrAbove(String choice) {
        String c = upper(choice);
        if (c == null) return false;
        return switch (c) {
            case "A" -> false;             // None
            case "B", "C", "D" -> true;    // Basic or above
            default -> false;
        };
    }

    /** A->0, B->1,...; returns null if out of range. */
    private static Integer mapChoiceIndex(String choice, int size) {
        String c = upper(choice);
        if (c == null || c.length() != 1) return null;
        int idx = c.charAt(0) - 'A';
        return (idx >= 0 && idx < size) ? idx : null;
    }

    private static String prettyEdu(int code) {
        return switch (code) {
            case 0 -> "No schooling";
            case 1 -> "8th";
            case 2 -> "10th";
            case 3 -> "12th";
            case 4 -> "Diploma/ITI";
            case 5 -> "Graduate";
            case 6 -> "Post‑grad+";
            default -> String.valueOf(code);
        };
    }
}