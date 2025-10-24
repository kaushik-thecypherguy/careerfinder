package com.acf.careerfinder.web;

import org.springframework.stereotype.Component;
import com.acf.careerfinder.psychometrics.Domain;
import com.acf.careerfinder.psychometrics.Trait;

@Component("labels")
public class Labels {
    public String trait(Trait t) {
        return t == null ? "" : t.label();
    }
    public String domain(Domain d) {
        if (d == null) return "";
        return switch (d) {
            case O  -> "Openness";
            case C  -> "Conscientiousness";
            case E  -> "Extraversion";
            case A  -> "Agreeableness";
            case ES -> "Emotional Stability";
        };
    }
}
