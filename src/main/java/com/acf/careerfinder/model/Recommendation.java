package com.acf.careerfinder.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Recommendation {

    private String title;
    private String summary;

    // Allow AI to send titles too (optional)
    private String careersTitle = "Suggested careers/trades";
    private String nextTitle = "Next steps";
    private String answersTitle = "Your answers";

    /** Careers (tolerates "careers" or "careerOptions" too) */
    @JsonProperty("suggestedCareers")
    @JsonAlias({"careers", "careerOptions", "career_list"})
    private List<Link> suggestedCareers;

    /** Steps (tolerates common synonyms) */
    @JsonAlias({"steps", "actionItems"})
    private List<String> nextSteps;

    /** Legacy echo (your old flow). Kept for fallback only, not populated by AI JSON. */
    @JsonIgnore
    private List<Map.Entry<String, String>> echoAnswers;

    /** New: AI-prepared Q&A rows in the user's language. */
    @JsonProperty("answersEcho")
    private List<AnswerRow> answersEcho;

    /* ------------------------ nested types ------------------------ */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        private String label;

        @JsonProperty("href")
        @JsonAlias({"url", "link"})
        private String href;

        public Link() { }
        public Link(String label, String href) { this.label = label; this.href = href; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnswerRow {
        private String label;
        private String value;

        public AnswerRow() { }
        public AnswerRow(String label, String value) { this.label = label; this.value = value; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /* ------------------------ getters/setters ------------------------ */

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCareersTitle() { return careersTitle; }
    public void setCareersTitle(String careersTitle) { this.careersTitle = careersTitle; }

    public String getNextTitle() { return nextTitle; }
    public void setNextTitle(String nextTitle) { this.nextTitle = nextTitle; }

    public String getAnswersTitle() { return answersTitle; }
    public void setAnswersTitle(String answersTitle) { this.answersTitle = answersTitle; }

    public List<Link> getSuggestedCareers() { return suggestedCareers; }
    public void setSuggestedCareers(List<Link> suggestedCareers) { this.suggestedCareers = suggestedCareers; }

    public List<String> getNextSteps() { return nextSteps; }
    public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }

    public List<Map.Entry<String, String>> getEchoAnswers() { return echoAnswers; }
    public void setEchoAnswers(List<Map.Entry<String, String>> echoAnswers) { this.echoAnswers = echoAnswers; }

    public List<AnswerRow> getAnswersEcho() { return answersEcho; }
    public void setAnswersEcho(List<AnswerRow> answersEcho) { this.answersEcho = answersEcho; }
}

