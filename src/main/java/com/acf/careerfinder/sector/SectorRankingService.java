package com.acf.careerfinder.sector;

import com.acf.careerfinder.sector.model1.SectorGates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Phase-7: Compose scoring + gating into the exact lists you want.
 *
 * Rule:
 *   - TopEligible: highest scoring eligible sectors (Top N).
 *   - NearMiss (ineligible): include every ineligible sector whose score >=
 *     the lowest score among TopEligible (eligible cutoff). Unlimited count.
 */
@Service
public class SectorRankingService {

    private final SectorConfigService cfg;
    private final EligibilityService eligibility;
    private final SectorScoringService scoring;

    @Value("${sectors.topN:5}")
    private int topN;

    public SectorRankingService(SectorConfigService cfg,
                                EligibilityService eligibility,
                                SectorScoringService scoring) {
        this.cfg = cfg;
        this.eligibility = eligibility;
        this.scoring = scoring;
    }

    /** View row for the UI. */
    public static final class SectorView {
        public final String id;
        public final String name;
        public final double score;
        public final boolean eligible;
        public final List<String> reasons;      // empty when eligible
        public final List<String> topContrib;   // top-3 weights

        public SectorView(String id, String name, double score,
                          boolean eligible, List<String> reasons, List<String> topContrib) {
            this.id = id; this.name = name; this.score = score;
            this.eligible = eligible;
            this.reasons = (reasons == null) ? List.of() : List.copyOf(reasons);
            this.topContrib = (topContrib == null) ? List.of() : List.copyOf(topContrib);
        }
    }

    /** Composite result for the result page. */
    public static final class RankedResult {
        public final List<SectorView> topEligible;
        public final List<SectorView> nearMiss;     // ineligible but >= cutoff
        public final double eligibleCutoff;         // score_5th (or lowest among TopEligible)

        public RankedResult(List<SectorView> topEligible, List<SectorView> nearMiss, double eligibleCutoff) {
            this.topEligible = List.copyOf(topEligible);
            this.nearMiss = List.copyOf(nearMiss);
            this.eligibleCutoff = eligibleCutoff;
        }
    }

    /**
     * Main entry:
     * @param gateAnswers  map with keys "gate.Q1".."gate.Q24"
     * @param tScores01to12  map "T01".."T12" -> 0..100 (final trait scores)
     */
    public RankedResult build(Map<String, String> gateAnswers, Map<String, Double> tScores01to12) {
        // 1) derive candidate attrs
        var cand = eligibility.deriveCand(gateAnswers);

        // 2) scores for all (ignoring gates)
        var scored = scoring.computeAll(tScores01to12); // sorted desc

        // 3) build id->requirements
        Map<String, SectorGates.Requirements> reqById = cfg.gates().sectors().stream()
                .collect(Collectors.toMap(SectorGates.SectorGate::id, SectorGates.SectorGate::req));

        // 4) attach eligibility (reasons come fully from EligibilityService)
        List<SectorView> allViews = new ArrayList<>();
        for (var s : scored) {
            var req = reqById.get(s.id);
            var e   = eligibility.checkEligibility(cand, req); // returns numeric + boolean reasons
            allViews.add(new SectorView(s.id, s.name, s.score, e.eligible, e.reasons, s.topContrib));
        }

        // 5) Top-N eligible
        List<SectorView> eligibleOnly = allViews.stream()
                .filter(v -> v.eligible)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();

        List<SectorView> topEligible = eligibleOnly.stream()
                .limit(Math.max(1, topN))
                .toList();

        // Cut-off = lowest score in TopEligible (or 0.0 if none)
        double cutoff = topEligible.isEmpty()
                ? 0.0
                : topEligible.stream().mapToDouble(v -> v.score).min().orElse(0.0);

        // 6) Near-miss = ineligible with score >= cutoff (unbounded)
        List<SectorView> nearMiss = allViews.stream()
                .filter(v -> !v.eligible && v.score >= cutoff)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();

        return new RankedResult(topEligible, nearMiss, cutoff);
    }
}