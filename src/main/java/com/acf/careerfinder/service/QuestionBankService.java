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

    // View models for Thymeleaf
    public record OptionVM(String value, String label) {}
    public record QuestionView(String key, String text, String type, boolean required, List<OptionVM> options) {}
    public record PageView(int page, int totalPages, List<QuestionView> questions) {}

    public PageView loadPage(String lang, int page, int pageSize) {
        if (lang == null || lang.isBlank()) lang = "en";

        List<QItem> all = itemRepo.findActiveOrdered();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        page = Math.max(1, Math.min(page, totalPages));

        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, all.size());
        List<QItem> window = all.subList(from, to);

        List<Long> ids = window.stream().map(QItem::getId).toList();

        // current language + English fallback
        Map<Long, QItemLocale> priMap = localeRepo.findByItemIdInAndLocale(ids, lang)
                .stream().collect(Collectors.toMap(l -> l.getItem().getId(), Function.identity()));

        Map<Long, QItemLocale> enMap = localeRepo.findByItemIdInAndLocale(ids, "en")
                .stream().collect(Collectors.toMap(l -> l.getItem().getId(), Function.identity()));

        List<QuestionView> out = new ArrayList<>(window.size());
        for (QItem it : window) {
            QItemLocale pri = priMap.get(it.getId());
            QItemLocale en  = enMap.get(it.getId());

            String text = pickText(pri, en, it.getQkey());
            List<OptionVM> opts = pickOptions(pri, en);

            String type = it.getQtype().name().toLowerCase(Locale.ROOT); // "single" | "multi" | "text"
            boolean required = Boolean.TRUE.equals(it.getRequired());

            out.add(new QuestionView(it.getQkey(), text, type, required, opts));
        }
        return new PageView(page, totalPages, out);
    }

    private static boolean isBlankish(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.isEmpty() || "—".equals(t) || "[]".equals(t);
    }

    private String pickText(QItemLocale pri, QItemLocale en, String dkey) {
        if (pri != null && !isBlankish(pri.getQuestionText())) return pri.getQuestionText();
        if (en  != null && !isBlankish(en.getQuestionText()))  return en.getQuestionText();
        return dkey; // last fallback
    }

    private List<OptionVM> pickOptions(QItemLocale pri, QItemLocale en) {
        List<OptionVM> fromPri = parseOptions(pri != null ? pri.getOptionsJson() : null);
        if (!fromPri.isEmpty()) return fromPri;
        return parseOptions(en != null ? en.getOptionsJson() : null);
    }

    private List<OptionVM> parseOptions(String json) {
        if (isBlankish(json)) return Collections.emptyList();
        try {
            List<Map<String, String>> raw = mapper.readValue(
                    json, new TypeReference<List<Map<String, String>>>() {});
            List<OptionVM> out = new ArrayList<>(raw.size());
            for (Map<String, String> m : raw) {
                String v = Objects.toString(m.getOrDefault("value",""), "").trim();
                String lab = Objects.toString(m.getOrDefault("label",""), "").trim();
                if (!v.isBlank() && !lab.isBlank()) out.add(new OptionVM(v, lab));
            }
            return out;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}