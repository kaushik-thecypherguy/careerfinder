package com.acf.careerfinder.service;

import com.acf.careerfinder.model.UserProgress;
import com.acf.careerfinder.model.UserProgress.Section;
import com.acf.careerfinder.repository.UserProgressRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class ProgressService {

    @Autowired private UserProgressRepository repo;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final TypeReference<Map<String,String>> MAP_TYPE = new TypeReference<>(){};

    @Transactional(Transactional.TxType.SUPPORTS) // read-only boundary (safe for future)
    public Optional<UserProgress> load(String email, Section section) {
        return repo.findByUserEmailAndSection(email, section);
    }

    public Map<String,String> toMap(String json) {
        try { return mapper.readValue(json == null ? "{}" : json, MAP_TYPE); }
        catch (Exception e) { return new TreeMap<>(); }
    }

    public String toJson(Map<String,String> map) {
        try { return mapper.writeValueAsString(map == null ? Map.of() : map); }
        catch (Exception e) { return "{}"; }
    }

    /** Merge delta answers + set page (upsert). */
    @Transactional
    public UserProgress upsertMerge(String email, Section section, Map<String,String> delta, Integer page) {
        UserProgress up = repo.findByUserEmailAndSection(email, section).orElseGet(() -> {
            UserProgress np = new UserProgress();
            np.setUserEmail(email);
            np.setSection(section);
            return np;
        });

        Map<String,String> merged = new TreeMap<>(toMap(up.getAnswersJson()));
        if (delta != null) {
            delta.forEach((k,v) -> {
                if (v == null || v.isBlank()) merged.remove(k);
                else merged.put(k, v);
            });
        }
        up.setAnswersJson(toJson(merged));
        if (page != null && page >= 1) up.setPage(page);

        return repo.save(up);
    }

    /** Mark the section as finished. */
    @Transactional
    public void markCompleted(String email, Section section) {
        UserProgress up = repo.findByUserEmailAndSection(email, section).orElseGet(() -> {
            UserProgress np = new UserProgress();
            np.setUserEmail(email);
            np.setSection(section);
            return np;
        });
        up.setCompleted(true);
        repo.save(up);
    }
}