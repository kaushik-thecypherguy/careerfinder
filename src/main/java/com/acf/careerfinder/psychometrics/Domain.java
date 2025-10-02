package com.acf.careerfinder.psychometrics;

/** Big-Five domains used by IPIP; ES = Emotional Stability (reverse of Neuroticism). */
public enum Domain {
    O("Openness / Intellect"),
    C("Conscientiousness"),
    E("Extraversion"),
    A("Agreeableness"),
    ES("Emotional Stability");

    private final String label;
    Domain(String label) { this.label = label; }
    public String label() { return label; }
}