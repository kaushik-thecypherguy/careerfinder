package com.acf.careerfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "q_item_locale",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_q_item_locale",
                columnNames = {"q_item_id", "locale"}
        ))
public class QItemLocale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to QItem
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "q_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_q_item_locale_q_item"))
    private QItem item;

    @Column(name = "locale", nullable = false, length = 5)
    private String locale; // "en" | "hi" | "mr"

    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;

    @Column(name = "options_json", columnDefinition = "text")
    private String optionsJson;   // JSON string of [{value,label}]

    @Column(name = "help_text", columnDefinition = "text")
    private String helpText;

    // --- getters/setters ---
    public Long getId() { return id; }

    public QItem getItem() { return item; }
    public void setItem(QItem item) { this.item = item; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
}