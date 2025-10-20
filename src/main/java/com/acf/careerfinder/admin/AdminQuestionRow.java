package com.acf.careerfinder.admin;

public class AdminQuestionRow {
    private final int serial;
    private final String qkey;
    private final String sectionKey;
    private final Integer orderIndex;
    private final String qtype;
    private final boolean required;
    private final boolean active;
    private final String metaSummary;
    private final String questionText;
    public AdminQuestionRow(int serial, String qkey, String sectionKey, Integer orderIndex,
                            String qtype, boolean required, boolean active,
                            String metaSummary, String questionText) {
        this.serial = serial; this.qkey = qkey; this.sectionKey = sectionKey; this.orderIndex = orderIndex;
        this.qtype = qtype; this.required = required; this.active = active;
        this.metaSummary = metaSummary; this.questionText = questionText;
    }
    public int getSerial() { return serial; }
    public String getQkey() { return qkey; }
    public String getSectionKey() { return sectionKey; }
    public Integer getOrderIndex() { return orderIndex; }
    public String getQtype() { return qtype; }
    public boolean isRequired() { return required; }
    public boolean isActive() { return active; }
    public String getMetaSummary() { return metaSummary; }
    public String getQuestionText() { return questionText; }
}