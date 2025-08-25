package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    Optional<QuestionnaireResponse> findByUserEmailAndQuestionKey(String userEmail, String questionKey);
    List<QuestionnaireResponse> findAllByUserEmail(String userEmail);
    void deleteAllByUserEmail(String userEmail);
}




