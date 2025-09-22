package com.acf.careerfinder.model;

import jakarta.persistence.*;

/*
@Entity
@Table(
        name = "questions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lang", "serial_no"}),
                @UniqueConstraint(columnNames = {"lang", "qkey"})
        }
)


public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qkey", nullable = false, length = 64)
    private String key;              // stable key across languages (e.g. "q.caste")

    @Column(name = "serial_no", nullable = false)
    private Integer serialNo;        // 1..N (order)

    @Column(name = "lang", nullable = false, length = 2)
    private String lang;             // "en","hi","mr"

    @Column(name = "qtext", nullable = false, columnDefinition = "text")
    private String text;             // localized question text

    @Enumerated(EnumType.STRING)
    @Column(name = "qtype", nullable = false, length = 16)
    private QuestionType type;       // RADIO | CHECKBOX | TEXT

    @Column(name = "options", columnDefinition = "text")
    private String options;          // "value|label;value|label;..."

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // ---- getters & setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public Integer getSerialNo() { return serialNo; }
    public void setSerialNo(Integer serialNo) { this.serialNo = serialNo; }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}*/

