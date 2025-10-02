package com.acf.careerfinder.psychometrics;

import java.util.Map;

/** Shape of q_item.meta_json. Fields are optional depending on kind. */
public record QuestionMeta(
        String kind,                 // "IPIP" or "SJT"
        String domain,               // IPIP: "O","C","E","A","ES"
        String keyed,                // "+" or "-"
        String scale,                // e.g., "Likert5"
        String category,             // optional tag
        String format,               // SJT: "MULTI_SELECT","YES_NO","SINGLE_BEST"
        String trait,                // SJT: "T01".."T12"
        Map<String,String> tagByValue, // SJT MULTI: { "a":"E", "b":"O", ... }
        String correctValue          // SJT YES/NO or SINGLE_BEST: "yes"/"no" or "a"/"b"/...
) {}