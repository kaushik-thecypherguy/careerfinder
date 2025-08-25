package com.acf.careerfinder.service;

import com.acf.careerfinder.model.QuestionnaireResponse;
import com.acf.careerfinder.repository.QuestionnaireResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class QuestionnaireService {

    @Autowired private QuestionnaireResponseRepository repo;

    /** Upsert each (question -> answer) row for this user into Postgres */
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
            repo.save(row); // persists to Postgres (pgAdmin4 will show it)
        }
    }

    /** Load all answers for a user as a Map<String,String> */
    public Map<String, String> loadAnswersMap(String email) {
        Map<String, String> map = new LinkedHashMap<>();
        for (QuestionnaireResponse r : repo.findAllByUserEmail(email)) {
            map.put(r.getQuestionKey(), r.getAnswerValue());
        }
        return map;
    }

    public void deleteAllForUser(String email) {
        repo.deleteAllByUserEmail(email);
    }
}
