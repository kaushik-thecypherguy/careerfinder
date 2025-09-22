package com.acf.careerfinder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

/*

@Service
public class QuestionQueryService {

    private final QuestionRepository repo;

    public QuestionQueryService(QuestionRepository repo) {
        this.repo = repo;
    }

    public List<QuestionVM> getQuestionsForLang(String lang) {
        if (lang == null || lang.isBlank()) lang = "en";
        List<Question> list = repo.findAllByLangAndActiveIsTrueOrderBySerialNoAsc(lang);
        List<QuestionVM> out = new ArrayList<>(list.size());
        for (Question q : list) out.add(toVM(q));
        return out;
    }

    private QuestionVM toVM(Question q) {
        return new QuestionVM(
                q.getSerialNo(),
                q.getKey(),
                q.getText(),
                q.getType().name(),   // "RADIO" | "CHECKBOX" | "TEXT"
                q.isRequired(),
                parseOptions(q.getOptions())
        );
    }

    private List<OptionVM> parseOptions(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        String[] items = raw.split(";");
        List<OptionVM> out = new ArrayList<>(items.length);
        for (String item : items) {
            if (item == null || item.isBlank()) continue;
            String[] kv = item.split("\\|", 2);
            String value = kv[0].trim();
            String label = kv.length > 1 ? kv[1].trim() : value;
            out.add(new OptionVM(value, label));
        }
        return out;
    }

    // simple VMs for the thymeleaf view
    public static record QuestionVM(Integer serialNo, String key, String text,
                                    String type, boolean required, List<OptionVM> options) {}
    public static record OptionVM(String value, String label) {}
}*/

