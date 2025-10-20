package com.acf.careerfinder.service;

import com.acf.careerfinder.model.QuestionnaireResponse;
import com.acf.careerfinder.repository.QuestionnaireResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class QuestionnaireService {

    private final QuestionnaireResponseRepository repo;
    public QuestionnaireService(QuestionnaireResponseRepository repo) { this.repo = repo; }

    @Transactional
    public void saveAnswers(String email, Map<String, String> answers) {
        if (answers == null) return;
        for (Map.Entry<String, String> e : answers.entrySet()) {
            String key = e.getKey();
            if (key == null || key.isBlank()) continue;
            String val = e.getValue() == null ? "" : e.getValue();
            QuestionnaireResponse row = repo
                    .findByUserEmailAndQuestionKey(email, key)
                    .orElseGet(() -> new QuestionnaireResponse(email, key));
            row.setAnswerValue(val);
            repo.save(row);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> loadAnswersMap(String email) {
        Map<String, String> map = new LinkedHashMap<>();
        for (QuestionnaireResponse r : repo.findAllByUserEmail(email)) {
            map.put(r.getQuestionKey(), r.getAnswerValue());
        }
        return map;
    }

    @Transactional
    public void deleteAllForUser(String email) {
        repo.deleteAllByUserEmail(email);
    }
}