package com.acf.careerfinder.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_section", columnNames = {"user_email", "section"})
)
public class UserProgress {

    public enum Section { GATING, QUESTIONNAIRE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 320)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "section", nullable = false, length = 20)
    private Section section;

    /** Store as plain TEXT, not a LOB/CLOB. */
    @Column(name = "answers_json", nullable = false, columnDefinition = "TEXT")
    private String answersJson = "{}";

    /** Last visited page (1‑based) */
    @Column(name = "page", nullable = false)
    private int page = 1;

    /** Whether the section is fully finished */
    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}