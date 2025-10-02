package com.acf.careerfinder.psychometrics;

/**
 * Constants used by scoring. No IO or wiring here.
 * - OCEAN -> 12-trait mapping matrix (row: T01..T12, col: O,C,E,A,ES)
 * - Blend weights for final trait score
 * - SJT normalization points
 */
public final class ScoringConfig {
    private ScoringConfig() {}

    /** Column order for the matrix is fixed: O, C, E, A, ES. */
    public static final Domain[] DOMS = { Domain.O, Domain.C, Domain.E, Domain.A, Domain.ES };

    /** Row order is fixed to Trait enum declaration order (T01..T12). */
    public static final double[][] OCEAN_TO_TRAIT = {
            /* T01_SAFETY        */ {0.0, 0.6, 0.0, 0.0, 0.4},
            /* T02_TEAMWORK      */ {0.0, 0.0, 0.4, 0.6, 0.0},
            /* T03_COMMUNICATION */ {0.0, 0.3, 0.7, 0.0, 0.0},
            /* T04_SERVICE       */ {0.0, 0.4, 0.0, 0.6, 0.0},
            /* T05_PRO_DEMEANOUR */ {0.0, 0.6, 0.0, 0.0, 0.4},
            /* T06_COMPLIANCE    */ {0.0, 0.7, 0.0, 0.3, 0.0},
            /* T07_DOCUMENTATION */ {0.2, 0.8, 0.0, 0.0, 0.0},
            /* T08_ATTENTION     */ {0.2, 0.8, 0.0, 0.0, 0.0},
            /* T09_PLANNING      */ {0.0, 0.8, 0.0, 0.0, 0.2},
            /* T10_ADAPTABILITY  */ {0.8, 0.2, 0.0, 0.0, 0.0},
            /* T11_DEESCALATION  */ {0.0, 0.0, 0.0, 0.3, 0.7},
            /* T12_INCLUSIVITY   */ {0.3, 0.0, 0.0, 0.7, 0.0}
    };

    /** Final blend weights (v1 policy). */
    public static final double W_IPIP = 0.60;
    public static final double W_SJT  = 0.40;

    /** Likert range for IPIP items (1..5). */
    public static final int IPIP_MIN = 1;
    public static final int IPIP_MAX = 5;

    /** SJT points per tag when MULTI_SELECT is used. */
    public static final int SJT_POINTS_E = 2;
    public static final int SJT_POINTS_O = 1;
    public static final int SJT_POINTS_X = -1;

    /** Scale factor: MULTI_SELECT normalized 0..1, then *4 to get 0..4 per item. */
    public static final double SJT_MULT_SELECT_SCALE = 4.0;

    // --- Friendly labels for UI (static helpers callable from Thymeleaf) ---
    public static String domainLabel(Domain d) {
        return switch (d) {
            case O  -> "Openness / Intellect";
            case C  -> "Conscientiousness";
            case E  -> "Extraversion";
            case A  -> "Agreeableness";
            case ES -> "Emotional Stability";
        };
    }

    public static String traitLabel(Trait t) {
        return switch (t) {
            case T01_SAFETY        -> "T01 Safety & Hygiene";
            case T02_TEAMWORK      -> "T02 Teamwork & Cooperation";
            case T03_COMMUNICATION -> "T03 Communication";
            case T04_SERVICE       -> "T04 Service Orientation";
            case T05_PRO_DEMEANOUR -> "T05 Professional Demeanour";
            case T06_COMPLIANCE    -> "T06 Compliance & Ethics";
            case T07_DOCUMENTATION -> "T07 Documentation";
            case T08_ATTENTION     -> "T08 Attention to Detail";
            case T09_PLANNING      -> "T09 Planning & Self‑Mgmt";
            case T10_ADAPTABILITY  -> "T10 Adaptability & Learning";
            case T11_DEESCALATION  -> "T11 De‑escalation / Stress";
            case T12_INCLUSIVITY   -> "T12 Inclusivity & Sensitivity";
        };
    }
}