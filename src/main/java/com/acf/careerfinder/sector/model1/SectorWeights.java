package com.acf.careerfinder.sector.model1;

import java.util.List;
import java.util.Map;

/**
 * weights: JSON object with keys "T01".."T12" mapped to normalized doubles.
 * Row-sum must be 1.0 ± tolerance.
 */
public record SectorWeights(List<Row> sectors) {
    public record Row(String id, Map<String, Double> weights) {}
}