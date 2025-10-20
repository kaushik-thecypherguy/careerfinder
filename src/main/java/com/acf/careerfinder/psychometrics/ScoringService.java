package com.acf.careerfinder.psychometrics;

import com.acf.careerfinder.model.QItem;
import com.acf.careerfinder.repository.QItemRepository;
import com.acf.careerfinder.service.QuestionnaireService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.acf.careerfinder.psychometrics.Domain.*;
import static com.acf.careerfinder.psychometrics.ScoringConfig.*;
import static com.acf.careerfinder.psychometrics.Trait.*;

/**
 * Computes TraitFinal(0–100) strictly from:
 *  - IPIP (Likert 1..5, reverse-key where keyed="-")
 *  - SJT  (MULTI_SELECT normalized; YES_NO/SINGLE_BEST => 0 or full scale)
 *  - OCEAN -> 12 traits (matrix)
 *  - Blend: W_IPIP * from-IPIP + W_SJT * from-SJT
 *
 * Items are ignored unless:
 *   - sectionKey ∈ {"ipip","sjt"}
 *   - meta.kind ∈ {"IPIP","SJT"} and required meta fields are valid.
 */
@Service
public class ScoringService {

    private final QItemRepository qItemRepo;
    private final QuestionnaireService questionnaireService;

    private static final ObjectMapper M = new ObjectMapper();

    public ScoringService(QItemRepository qItemRepo,
                          QuestionnaireService questionnaireService) {
        this.qItemRepo = qItemRepo;
        this.questionnaireService = questionnaireService;
    }

    /** Public entry: compute for a user email. */
    public TraitProfile scoreForUser(String email) {
        // 1) Fetch
        List<QItem> items = qItemRepo.findActiveOrdered();
        Map<String, String> answers = questionnaireService.loadAnswersMap(email);

        // 2) Accumulators
        EnumMap<Domain, Integer> ipipSum = new EnumMap<>(Domain.class);
        EnumMap<Domain, Integer> ipipCount = new EnumMap<>(Domain.class);
        for (Domain d : Domain.values()) { ipipSum.put(d, 0); ipipCount.put(d, 0); }

        EnumMap<Trait, List<Double>> sjtBuckets = new EnumMap<>(Trait.class);
        for (Trait t : Trait.values()) sjtBuckets.put(t, new ArrayList<>());

        // 3) Walk each active item (strict filtering)
        for (QItem qi : items) {
            String sect = qi.getSectionKey();
            if (sect == null || !(sect.equalsIgnoreCase("ipip") || sect.equalsIgnoreCase("sjt"))) {
                continue; // hard block anything not IPIP/SJT
            }

            String metaJson = qi.getMetaJson();
            if (metaJson == null || metaJson.isBlank()) continue;

            JsonNode meta = parse(metaJson);
            if (meta == null) continue;

            String kind = text(meta, "kind"); // "IPIP" | "SJT"
            if ("IPIP".equalsIgnoreCase(kind)) {
                // Expected: SINGLE Likert 1..5, domain in O/C/E/A/ES
                if (qi.getQtype() != null && qi.getQtype() != QItem.QType.SINGLE) continue;
                String domStr = text(meta, "domain");
                Domain dom = parseDomain(domStr);
                if (dom == null) continue;

                String qkey = qi.getQkey();
                String val = answers.get(qkey);
                Integer likert = parseLikert(val);  // 1..5 or null
                if (likert == null) continue;

                boolean keyedPos = !"-".equals(text(meta, "keyed")); // default "+"
                int scored = keyedPos ? likert : (6 - likert);
                ipipSum.put(dom, ipipSum.get(dom) + scored);
                ipipCount.put(dom, ipipCount.get(dom) + 1);

            } else if ("SJT".equalsIgnoreCase(kind)) {
                // Expected: MULTI_SELECT|YES_NO|SINGLE_BEST with trait "Txx"
                String fmt = text(meta, "format");
                Trait trait = parseTrait(text(meta, "trait"));
                if (trait == null) continue;

                String qkey = qi.getQkey();
                String answer = answers.get(qkey);
                double item0toScale = 0.0;

                if ("MULTI_SELECT".equalsIgnoreCase(fmt)) {
                    if (qi.getQtype() != null && qi.getQtype() != QItem.QType.MULTI) continue;
                    Map<String, String> tagMap = parseTagMap(meta.get("tagByValue"));
                    if (tagMap == null || tagMap.isEmpty()) continue;

                    Set<String> selected = splitCsv(answer);
                    int eTotal = 0, oTotal = 0, points = 0;

                    for (String v : tagMap.keySet()) {
                        String tag = tagMap.get(v);
                        if ("E".equalsIgnoreCase(tag)) eTotal++;
                        if ("O".equalsIgnoreCase(tag)) oTotal++;
                    }
                    for (String v : selected) {
                        String tag = tagMap.get(v);
                        if (tag == null) continue;
                        switch (tag.toUpperCase(Locale.ROOT)) {
                            case "E" -> points += SJT_POINTS_E;
                            case "O" -> points += SJT_POINTS_O;
                            case "X" -> points += SJT_POINTS_X;
                        }
                    }
                    int denom = (SJT_POINTS_E * eTotal) + (SJT_POINTS_O * oTotal);
                    if (denom > 0) {
                        double norm01 = Math.max(points, 0) / (double) denom;
                        item0toScale = SJT_MULT_SELECT_SCALE * norm01; // 0..4
                    }

                } else if ("YES_NO".equalsIgnoreCase(fmt) || "SINGLE_BEST".equalsIgnoreCase(fmt)) {
                    if (qi.getQtype() != null && qi.getQtype() == QItem.QType.MULTI) continue;
                    String correct = Optional.ofNullable(text(meta, "correctValue")).orElse("");
                    String given   = Optional.ofNullable(answer).orElse("");
                    if (!correct.isBlank()) {
                        item0toScale = correct.equalsIgnoreCase(given) ? SJT_MULT_SELECT_SCALE : 0.0;
                    }
                } else {
                    continue; // unsupported format
                }

                sjtBuckets.get(trait).add(item0toScale);
            }
            // else: unknown kind → ignore
        }

        // 4) Build profile
        TraitProfile profile = new TraitProfile();

        // --- 4a) IPIP → domains 0..100 (weighted by answered count) ---
        EnumMap<Domain, Integer> ipipRaw = new EnumMap<>(Domain.class);
        EnumMap<Domain, Double>  ocean0100 = new EnumMap<>(Domain.class);
        int totalIpipN = 0;
        double weightedSum = 0.0;

        for (Domain d : Domain.values()) {
            int n = ipipCount.get(d);
            int raw = ipipSum.get(d);
            ipipRaw.put(d, raw);

            double pct;
            if (n <= 0) {
                pct = 0.0;
            } else {
                // min n*1, max n*5, range 4n ⇒ 0..100
                pct = 100.0 * (raw - n) / (4.0 * n);
                pct = clamp100(pct);
                weightedSum += pct * n;
                totalIpipN += n;
            }
            ocean0100.put(d, pct);
        }
        profile.ipipRaw().putAll(ipipRaw);
        profile.ocean0to100().putAll(ocean0100);
        profile.setIpipOverall0to100(totalIpipN > 0 ? weightedSum / totalIpipN : 0.0);

        // --- 4b) OCEAN -> 12 traits (from IPIP) ---
        EnumMap<Trait, Double> traitsFromIpip = new EnumMap<>(Trait.class);
        for (int r = 0; r < Trait.values().length; r++) {
            double sum = 0.0;
            for (int c = 0; c < DOMS.length; c++) {
                double dVal = ocean0100.getOrDefault(DOMS[c], 0.0);
                sum += OCEAN_TO_TRAIT[r][c] * dVal;
            }
            traitsFromIpip.put(Trait.values()[r], clamp100(sum));
        }
        profile.traitFromIpip0to100().putAll(traitsFromIpip);

        // --- 4c) SJT -> 12 traits (mean item 0..scale ⇒ 0..100) ---
        EnumMap<Trait, Double> traitSjt = new EnumMap<>(Trait.class);
        int totalSjtItems = 0;
        double sumItems0toScale = 0.0;

        for (Trait t : Trait.values()) {
            List<Double> arr = sjtBuckets.get(t);
            double mean = (arr == null || arr.isEmpty())
                    ? 0.0
                    : arr.stream().mapToDouble(d -> d).average().orElse(0.0); // 0..scale
            double out0100 = (100.0 / SJT_MULT_SELECT_SCALE) * mean;
            out0100 = clamp100(out0100);
            traitSjt.put(t, out0100);

            if (arr != null) {
                totalSjtItems += arr.size();
                sumItems0toScale += arr.stream().mapToDouble(d -> d).sum();
            }
        }
        profile.traitSjt0to100().putAll(traitSjt);
        profile.setSjtOverall0to100(
                totalSjtItems > 0 ? clamp100((100.0 / SJT_MULT_SELECT_SCALE) * (sumItems0toScale / totalSjtItems)) : 0.0
        );

        // --- 4d) Blend final per-trait + composite ---
        EnumMap<Trait, Double> traitFinal = new EnumMap<>(Trait.class);
        for (Trait t : Trait.values()) {
            double a = traitsFromIpip.getOrDefault(t, 0.0);
            double b = traitSjt.getOrDefault(t, 0.0);
            double f = W_IPIP * a + W_SJT * b;
            traitFinal.put(t, clamp100(f));
        }
        profile.traitFinal0to100().putAll(traitFinal);

        double composite = W_IPIP * profile.getIpipOverall0to100()
                + W_SJT  * profile.getSjtOverall0to100();
        profile.setComposite0to100(clamp100(composite));

        return profile;
    }

    /* -------------------- STATIC label helpers for Thymeleaf -------------------- */

    /** Used from result.html: T(ScoringService).traitLabel(e.key) */
    public static String traitLabel(Trait t) {
        return (t == null) ? "" : t.label();
    }

    /** Used from result.html: T(ScoringService).domainLabel(d.key) */
    public static String domainLabel(Domain d) {
        if (d == null) return "";
        return switch (d) {
            case O  -> "Openness";
            case C  -> "Conscientiousness";
            case E  -> "Extraversion";
            case A  -> "Agreeableness";
            case ES -> "Emotional Stability";
        };
    }

    /* -------------------- Internals -------------------- */
    private static JsonNode parse(String json) { try { return M.readTree(json); } catch (Exception e) { return null; } }
    private static String  text(JsonNode n, String k) { JsonNode x = n.get(k); return (x==null||x.isNull()) ? null : x.asText(); }
    private static double  clamp100(double v){ return v < 0 ? 0 : (v > 100 ? 100 : v); }

    private static Domain parseDomain(String s) {
        if (s == null) return null;
        return switch (s.toUpperCase(Locale.ROOT)) {
            case "O" -> O;
            case "C" -> C;
            case "E" -> E;
            case "A" -> A;
            case "ES" -> ES;
            default -> null;
        };
    }

    /** Accepts "T01", "t6", etc. */
    private static Trait parseTrait(String s) {
        if (s == null) return null;
        String num = s.toUpperCase(Locale.ROOT).replaceAll("[^0-9]", "");
        if (num.isBlank()) return null;
        int n = Integer.parseInt(num);
        Trait[] all = Trait.values();
        return (n >= 1 && n <= all.length) ? all[n - 1] : null;
    }

    private static Integer parseLikert(String s) {
        if (s == null) return null;
        try {
            int v = Integer.parseInt(s.trim());
            if (v >= IPIP_MIN && v <= IPIP_MAX) return v;
        } catch (Exception ignored) {}
        return null;
    }

    private static Set<String> splitCsv(String s) {
        if (s == null || s.isBlank()) return Collections.emptySet();
        return Arrays.stream(s.split(","))
                .map(x -> x.trim().toLowerCase(Locale.ROOT))
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Map<String, String> parseTagMap(JsonNode node) {
        if (node == null || !node.isObject()) return null;
        Map<String, String> out = new LinkedHashMap<>();
        node.fieldNames().forEachRemaining(k -> out.put(k.toLowerCase(Locale.ROOT), node.get(k).asText()));
        return out;
    }
}
