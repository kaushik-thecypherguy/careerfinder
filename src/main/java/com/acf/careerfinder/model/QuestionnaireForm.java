package com.acf.careerfinder.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestionnaireForm {
    private Map<String, String> answers = new LinkedHashMap<>();
    public QuestionnaireForm() {}
    public Map<String, String> getAnswers() { return answers; }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }
}



