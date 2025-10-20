package com.acf.careerfinder.service;

import com.acf.careerfinder.model.QItem;
import com.acf.careerfinder.model.QItemLocale;
import com.acf.careerfinder.repository.QItemLocaleRepository;
import com.acf.careerfinder.repository.QItemRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final QItemRepository itemRepo;
    private final QItemLocaleRepository localeRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuestionBankService(QItemRepository itemRepo, QItemLocaleRepository localeRepo) {
        this.itemRepo = itemRepo;
        this.localeRepo = localeRepo;
    }

    // --- View models exposed to Thymeleaf / controller ---
    public record OptionVM(String value, String label) {}
    public record QuestionView(String key, String text, String type, boolean required, List<OptionVM> options) {}
    public record PageView(int page, int totalPages, int pageSize, List<QuestionView> questions) {}

    /** Load one page of localized questions from the active bank (IPIP/SJT only). */
    public PageView loadPage(String lang, int page, int pageSize) {
        if (lang == null || lang.isBlank()) lang = "en";

        // Load ALL active questions then hard-filter to IPIP/SJT for safety.
        List<QItem> allActive = itemRepo.findActiveOrdered();
        List<QItem> all = allActive.stream()
                .filter(q -> {
                    String s = q.getSectionKey();
                    return s != null && (s.equalsIgnoreCase("ipip") || s.equalsIgnoreCase("sjt"));
                })
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        page = Math.max(1, Math.min(page, totalPages));

        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, all.size());
        List<QItem> window = (from < to) ? all.subList(from, to) : List.of();

        if (window.isEmpty()) {
            return new PageView(page, totalPages, pageSize, List.of());
        }

        // Preload locales for the window
        List<Long> ids = window.stream().map(QItem::getId).toList();

        Map<Long, QItemLocale> priMap = localeRepo.findByItemIdInAndLocale(ids, lang).stream()
                .collect(Collectors.toMap(l -> l.getItem().getId(), Function.identity()));

        Map<Long, QItemLocale> enMap = "en".equals(lang) ? Collections.emptyMap()
                : localeRepo.findByItemIdInAndLocale(ids, "en").stream()
                .collect(Collectors.toMap(l -> l.getItem().getId(), Function.identity()));

        // Build question VMs
        List<QuestionView> out = new ArrayList<>();
        for (QItem it : window) {
            QItemLocale pri = priMap.get(it.getId());
            QItemLocale en  = enMap.get(it.getId());

            String text = pickText(pri, en, it.getQkey());
            List<OptionVM> opts = pickOptions(pri, en);

            String type = (it.getQtype() != null
                    ? it.getQtype().name()
                    : QItem.QType.SINGLE.name()).toLowerCase(Locale.ROOT); // "single"|"multi"|"text"

            boolean required = Boolean.TRUE.equals(it.getRequired());

            out.add(new QuestionView(it.getQkey(), text, type, required, opts));
        }

        return new PageView(page, totalPages, pageSize, out);
    }

    // -------------------- helpers --------------------
    private static boolean isBlankish(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.isEmpty() || "—".equals(t) || "-".equals(t) || "–".equals(t);
    }

    /** Prefer primary locale text, then English, then qkey. */
    private String pickText(QItemLocale pri, QItemLocale en, String fallbackKey) {
        if (pri != null && !isBlankish(pri.getQuestionText())) return pri.getQuestionText();
        if (en  != null && !isBlankish(en.getQuestionText()))  return en.getQuestionText();
        return fallbackKey;
    }

    /** Parse options JSON from primary locale or fall back to English. */
    private List<OptionVM> pickOptions(QItemLocale pri, QItemLocale en) {
        String json = null;
        if (pri != null && !isBlankish(pri.getOptionsJson())) json = pri.getOptionsJson();
        else if (en != null && !isBlankish(en.getOptionsJson())) json = en.getOptionsJson();
        if (isBlankish(json)) return Collections.emptyList();

        try {
            List<Map<String, String>> raw = mapper.readValue(json, new TypeReference<>(){});
            List<OptionVM> out = new ArrayList<>(raw.size());
            for (Map<String, String> m : raw) {
                String v = Objects.toString(m.get("value"), "").trim();
                String lab = Objects.toString(m.get("label"), "").trim();
                if (!v.isBlank() && !lab.isBlank()) out.add(new OptionVM(v, lab));
            }
            return out;
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }
}