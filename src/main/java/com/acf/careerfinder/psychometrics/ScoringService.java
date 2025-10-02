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
import static com.acf.careerfinder.psychometrics.Trait.*;
import static com.acf.careerfinder.psychometrics.ScoringConfig.*;

/**
 * Computes TraitFinal(0–100) from:
 *  - IPIP items (Likert 1..5, reverse-key where keyed = "-")
 *  - SJT items (MULTI_SELECT normalized to 0..4; YES_NO/MCQ => 0 or 4)
 *  - OCEAN -> 12 traits via OCEAN_TO_TRAIT matrix
 *  - Blend: W_IPIP * from-IPIP + W_SJT * from-SJT
 *
 * No controller or DB writes here. Pure read + math.
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

        // Optional: keep per-item diagnostics if you want later
        // Map<String, Double> sjtItem0to4 = new HashMap<>();

        // 3) Walk each active item
        for (QItem qi : items) {
            String metaJson = qi.getMetaJson();
            if (metaJson == null || metaJson.isBlank()) continue;

            String qkey = qi.getQkey();
            String val = answers.get(qkey); // can be null
            JsonNode meta = parse(metaJson);
            if (meta == null) continue;

            String kind = text(meta, "kind");           // "IPIP" | "SJT"
            if ("IPIP".equalsIgnoreCase(kind)) {
                handleIpip(meta, val, ipipSum, ipipCount);
            } else if ("SJT".equalsIgnoreCase(kind)) {
                handleSjt(meta, val, sjtBuckets);
            }
        }

        // 4) Build profile
        TraitProfile profile = new TraitProfile();

        // 4a) IPIP -> domains 0..100 (dynamic scaling by answered count)
        EnumMap<Domain, Integer> ipipRaw = new EnumMap<>(Domain.class);
        EnumMap<Domain, Double> ocean0100 = new EnumMap<>(Domain.class);
        for (Domain d : Domain.values()) {
            int n = ipipCount.get(d);
            int raw = ipipSum.get(d);
            ipipRaw.put(d, raw);
            double score;
            if (n <= 0) {
                score = 0.0;
            } else {
                // min = n*1 ; max = n*5 ; range = 4n
                score = 100.0 * (raw - n) / (4.0 * n);
                if (score < 0) score = 0;
                if (score > 100) score = 100;
            }
            ocean0100.put(d, score);
        }
        profile.ipipRaw().putAll(ipipRaw);
        profile.ocean0to100().putAll(ocean0100);

        // 4b) OCEAN -> 12 traits (from IPIP)
        EnumMap<Trait, Double> traitsFromIpip = new EnumMap<>(Trait.class);
        for (int r = 0; r < Trait.values().length; r++) {
            double sum = 0.0;
            for (int c = 0; c < DOMS.length; c++) {
                double dVal = ocean0100.getOrDefault(DOMS[c], 0.0);
                sum += OCEAN_TO_TRAIT[r][c] * dVal;
            }
            traitsFromIpip.put(Trait.values()[r], clamp01(sum));
        }
        profile.traitFromIpip0to100().putAll(traitsFromIpip);

        // 4c) SJT -> 12 traits (average items 0..4 => ×25 => 0..100)
        EnumMap<Trait, Double> traitSjt = new EnumMap<>(Trait.class);
        for (Trait t : Trait.values()) {
            List<Double> arr = sjtBuckets.get(t);
            double s = arr.isEmpty() ? 0.0 : arr.stream().mapToDouble(d -> d).average().orElse(0.0);
            double out0100 = 25.0 * s; // 0..4 -> 0..100
            traitSjt.put(t, clamp01(out0100));
        }
        profile.traitSjt0to100().putAll(traitSjt);

        // 4d) Blend to final
        EnumMap<Trait, Double> traitFinal = new EnumMap<>(Trait.class);
        for (Trait t : Trait.values()) {
            double a = traitsFromIpip.getOrDefault(t, 0.0);
            double b = traitSjt.getOrDefault(t, 0.0);
            double f = W_IPIP * a + W_SJT * b;
            traitFinal.put(t, clamp01(f));
        }
        profile.traitFinal0to100().putAll(traitFinal);

        return profile;
    }

    /* -------------------- Internals -------------------- */

    private static JsonNode parse(String json) {
        try {
            return M.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode n, String key) {
        JsonNode x = n.get(key);
        return x == null || x.isNull() ? null : x.asText();
    }

    private static double clamp01(double x) {
        if (x < 0) return 0;
        if (x > 100) return 100;
        return x;
    }

    /** IPIP handler: Likert 1..5, reverse if keyed = "-" */
    private static void handleIpip(JsonNode meta, String answer,
                                   EnumMap<Domain, Integer> sum,
                                   EnumMap<Domain, Integer> cnt) {
        String domStr = text(meta, "domain");  // "O","C","E","A","ES"
        Domain dom = parseDomain(domStr);
        if (dom == null) return;

        Integer likert = parseLikert(answer);  // 1..5 or null
        if (likert == null) return;

        boolean keyedPos = !"-".equals(text(meta, "keyed")); // default "+"
        int scored = keyedPos ? likert : (6 - likert);
        sum.put(dom, sum.get(dom) + scored);
        cnt.put(dom, cnt.get(dom) + 1);
    }

    /** SJT handler: MULTI_SELECT | YES_NO | SINGLE_BEST */
    private static void handleSjt(JsonNode meta, String answer,
                                  EnumMap<Trait, List<Double>> buckets) {
        Trait trait = parseTrait(text(meta, "trait")); // expects "T06" etc.
        if (trait == null) return;

        String fmt = text(meta, "format");
        double item0to4 = 0.0;

        if ("MULTI_SELECT".equalsIgnoreCase(fmt)) {
            // tags map: { "a":"E", "b":"O", "c":"X", ... }
            Map<String, String> tagMap = parseTagMap(meta.get("tagByValue"));
            if (tagMap == null || tagMap.isEmpty()) return;

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
                    case "E" -> points += 2;
                    case "O" -> points += 1;
                    case "X" -> points -= 1;
                }
            }
            int denom = 2 * eTotal + oTotal;
            if (denom > 0) {
                double norm = Math.max(points, 0) / (double) denom; // 0..1
                item0to4 = 4.0 * norm;
            } else {
                item0to4 = 0.0;
            }
        } else if ("YES_NO".equalsIgnoreCase(fmt) || "SINGLE_BEST".equalsIgnoreCase(fmt)) {
            String correct = Optional.ofNullable(text(meta, "correctValue")).orElse("");
            String given = Optional.ofNullable(answer).orElse("");
            item0to4 = correct.equalsIgnoreCase(given) ? 4.0 : 0.0;
        } else {
            return; // unknown SJT format
        }

        buckets.get(trait).add(item0to4);
        // sjtItem0to4.put(qkey, item0to4); // if you want per-item logs later
    }

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

    /** Trait from code like "T01", "T06" (we ignore suffix). */
    private static Trait parseTrait(String s) {
        if (s == null) return null;
        String code = s.toUpperCase(Locale.ROOT).replaceAll("[^0-9]", "");
        if (code.isBlank()) return null;
        int n = Integer.parseInt(code);
        Trait[] all = Trait.values();
        if (n >= 1 && n <= all.length) return all[n - 1];
        return null;
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