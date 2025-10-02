package com.acf.careerfinder.admin;

public class AdminQuestionDTO {
    // Core
    private String qkey;          // ipip.C.01 or sjt.T06.01
    private String sectionKey;    // "ipip" or "sjt"
    private String qtype;         // "SINGLE" or "MULTI"
    private Integer orderIndex;   // 1..N
    private boolean required;     // true/false
    private boolean active;       // true/false
    private String metaJson;      // JSON contract

    // Locales: question text + options JSON
    private String enQuestion;    private String enOptionsJson;
    private String hiQuestion;    private String hiOptionsJson;
    private String mrQuestion;    private String mrOptionsJson;

    // getters/setters
    public String getQkey() { return qkey; }
    public void setQkey(String qkey) { this.qkey = qkey; }
    public String getSectionKey() { return sectionKey; }
    public void setSectionKey(String sectionKey) { this.sectionKey = sectionKey; }
    public String getQtype() { return qtype; }
    public void setQtype(String qtype) { this.qtype = qtype; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }
    public String getEnQuestion() { return enQuestion; }
    public void setEnQuestion(String enQuestion) { this.enQuestion = enQuestion; }
    public String getEnOptionsJson() { return enOptionsJson; }
    public void setEnOptionsJson(String enOptionsJson) { this.enOptionsJson = enOptionsJson; }
    public String getHiQuestion() { return hiQuestion; }
    public void setHiQuestion(String hiQuestion) { this.hiQuestion = hiQuestion; }
    public String getHiOptionsJson() { return hiOptionsJson; }
    public void setHiOptionsJson(String hiOptionsJson) { this.hiOptionsJson = hiOptionsJson; }
    public String getMrQuestion() { return mrQuestion; }
    public void setMrQuestion(String mrQuestion) { this.mrQuestion = mrQuestion; }
    public String getMrOptionsJson() { return mrOptionsJson; }
    public void setMrOptionsJson(String mrOptionsJson) { this.mrOptionsJson = mrOptionsJson; }
}