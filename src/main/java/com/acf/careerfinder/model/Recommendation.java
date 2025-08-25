package com.acf.careerfinder.model;

import java.util.List;
import java.util.Map;

public class Recommendation {
    private String title;
    private String summary;
    private List<String> suggestedCareers;
    private List<String> nextSteps;
    private Map<String, String> echoAnswers;

    public Recommendation() {}

    public Recommendation(String title, String summary,
                          List<String> suggestedCareers,
                          List<String> nextSteps,
                          Map<String, String> echoAnswers) {
        this.title = title;
        this.summary = summary;
        this.suggestedCareers = suggestedCareers;
        this.nextSteps = nextSteps;
        this.echoAnswers = echoAnswers;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getSuggestedCareers() { return suggestedCareers; }
    public void setSuggestedCareers(List<String> suggestedCareers) { this.suggestedCareers = suggestedCareers; }

    public List<String> getNextSteps() { return nextSteps; }
    public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }

    public Map<String, String> getEchoAnswers() { return echoAnswers; }
    public void setEchoAnswers(Map<String, String> echoAnswers) { this.echoAnswers = echoAnswers; }
}

