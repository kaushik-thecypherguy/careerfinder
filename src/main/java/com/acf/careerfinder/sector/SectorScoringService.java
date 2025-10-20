package com.acf.careerfinder.sector;

import com.acf.careerfinder.psychometrics.Trait;
import com.acf.careerfinder.sector.model1.SectorCatalog;
import com.acf.careerfinder.sector.model1.SectorWeights;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Phase-7: Weighted-sum sector scoring.
 *
 * Supports both inputs:
 *  - Map<String, Double> keyed by "T01".."T12"
 *  - Map<Trait,  Double> keyed by the Trait enum
 *
 * The weights JSON is keyed by "T01".."T12" and each row is normalized.
 */
@Service
public class SectorScoringService {

    private final SectorConfigService cfg;

    public SectorScoringService(SectorConfigService cfg) {
        this.cfg = cfg;
    }

    /** Human labels for the 12 T-codes for "why fit". */
    private static final Map<String,String> T_NAMES = Map.ofEntries(
            Map.entry("T01", "Safety & Hygiene"),
            Map.entry("T02", "Teamwork & Cooperation"),
            Map.entry("T03", "Communication"),
            Map.entry("T04", "Service Orientation"),
            Map.entry("T05", "Professional Demeanour"),
            Map.entry("T06", "Compliance & Ethics"),
            Map.entry("T07", "Documentation"),
            Map.entry("T08", "Attention to Detail"),
            Map.entry("T09", "Planning & Self‑Mgmt"),
            Map.entry("T10", "Adaptability & Learning"),
            Map.entry("T11", "De‑escalation / Stress"),
            Map.entry("T12", "Inclusivity & Sensitivity")
    );

    /** Output row. */
    public static final class ScoredSector {
        public final String id;
        public final String name;
        public final double score;              // 0..100
        public final List<String> topContrib;   // top-3 trait labels by weight

        public ScoredSector(String id, String name, double score, List<String> topContrib) {
            this.id = id; this.name = name; this.score = score; this.topContrib = topContrib;
        }
    }

    /* ----------------------------------------------------------------------
     * PUBLIC APIs
     * ---------------------------------------------------------------------- */

    /** Old signature (kept for compatibility): input keyed by "T01".."T12". */
    public List<ScoredSector> computeAll(Map<String, Double> traitScoresByCode) {
        Objects.requireNonNull(traitScoresByCode, "trait scores map required");
        return computeUsingResolver(code -> traitScoresByCode.getOrDefault(code, 0.0));
    }

    /** New helper: input keyed by Trait enum (what TraitProfile returns). */
    public List<ScoredSector> computeAllFromEnum(Map<Trait, Double> traitScoresByEnum) {
        Objects.requireNonNull(traitScoresByEnum, "trait scores map (by enum) required");
        // Resolve code "Txx" -> Trait enum by ordinal (T01 = ordinal 0, etc.)
        return computeUsingResolver(code -> {
            int idx = parseCodeToIndex(code);
            if (idx < 0) return 0.0;
            Trait[] all = Trait.values();
            if (idx >= all.length) return 0.0;
            return traitScoresByEnum.getOrDefault(all[idx], 0.0);
        });
    }

    /* ----------------------------------------------------------------------
     * INTERNALS
     * ---------------------------------------------------------------------- */

    /** Common scoring implementation driven by a resolver for "Txx" -> value. */
    private List<ScoredSector> computeUsingResolver(java.util.function.Function<String, Double> tResolver) {
        SectorCatalog catalog = cfg.catalog();
        SectorWeights weights = cfg.weights();

        Map<String,String> nameById = catalog.sectors().stream()
                .collect(Collectors.toMap(SectorCatalog.Sector::id, SectorCatalog.Sector::name));

        List<ScoredSector> out = new ArrayList<>();
        for (SectorWeights.Row row : weights.sectors()) {
            double sum = 0.0;

            for (Map.Entry<String, Double> e : row.weights().entrySet()) {
                double w = e.getValue() == null ? 0d : e.getValue();
                double t = Optional.ofNullable(tResolver.apply(e.getKey())).orElse(0.0);
                sum += w * t; // t is already 0..100; row weights sum to ~1
            }

            // Top-3 contributors by weight (not by candidate's score)
            List<String> top3 = row.weights().entrySet().stream()
                    .sorted((a,b) -> Double.compare(
                            nz(b.getValue()), nz(a.getValue())))
                    .limit(3)
                    .map(Map.Entry::getKey)               // "Txx"
                    .map(k -> T_NAMES.getOrDefault(k, k)) // pretty label
                    .collect(Collectors.toList());

            out.add(new ScoredSector(
                    row.id(),
                    nameById.getOrDefault(row.id(), row.id()),
                    round1(sum),
                    top3
            ));
        }

        out.sort((a,b) -> Double.compare(b.score, a.score));
        return out;
    }

    private static double nz(Double v) { return v == null ? 0.0 : v; }

    /** "T01" -> 0, "T12" -> 11, invalid -> -1 */
    private static int parseCodeToIndex(String code) {
        if (code == null || code.length() != 3 || code.charAt(0) != 'T') return -1;
        try {
            int n = Integer.parseInt(code.substring(1)); // 1..12
            return n - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double round1(double x) { return Math.round(x * 10.0) / 10.0; }
}