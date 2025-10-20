package com.acf.careerfinder.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_aggregate")
public class QuestionnaireAggregate {

    @Id
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Lob
    @Column(name = "answers_json", nullable = false, columnDefinition = "TEXT")
    private String answersJson; // JSON of Map<String,String>

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public QuestionnaireAggregate() {}
    public QuestionnaireAggregate(String userEmail, String answersJson) {
        this.userEmail = userEmail;
        this.answersJson = answersJson;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
