package com.acf.careerfinder.sector;

import com.acf.careerfinder.psychometrics.Trait;
import com.acf.careerfinder.sector.model1.SectorCatalog;
import com.acf.careerfinder.sector.model1.SectorGates;
import com.acf.careerfinder.sector.model1.SectorWeights;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SectorConfigService {

    // Defaults assume Option B folder layout under src/main/resources/config/sectors/v1
    @Value("${sector.catalog.path:config/sectors/v1/SectorCatalog_v1.json}")
    private String catalogPath;

    @Value("${sector.gates.path:config/sectors/v1/SectorGates_v1.json}")
    private String gatesPath;

    @Value("${sector.weights.path:config/sectors/v1/SectorWeights_v2_3.json}")
    private String weightsPath;

    @Value("${sector.weights.rowTolerance:0.001}")
    private double rowTolerance;

    // Ignore unknown fields like "version", "asOf" in JSON headers
    private final ObjectMapper M = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private SectorCatalog catalog;
    private SectorGates gates;
    private SectorWeights weights;

    /* -------- Loaders -------- */

    public synchronized SectorCatalog catalog() {
        if (catalog == null) catalog = readJson(catalogPath, SectorCatalog.class);
        return catalog;
    }

    public synchronized SectorGates gates() {
        if (gates == null) gates = readJson(gatesPath, SectorGates.class);
        return gates;
    }

    public synchronized SectorWeights weights() {
        if (weights == null) weights = readJson(weightsPath, SectorWeights.class);
        return weights;
    }

    private <T> T readJson(String path, Class<T> type) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return M.readValue(in, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JSON: " + path, e);
        }
    }

    /* -------- Validation (used by SectorConfigValidator) -------- */

    public ValidationReport validateAll() {
        SectorCatalog cat = catalog();
        SectorGates gat = gates();
        SectorWeights w = weights();

        Set<String> idsCat = cat.sectors().stream()
                .map(SectorCatalog.Sector::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> idsGat = gat.sectors().stream()
                .map(SectorGates.SectorGate::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> idsWei = w.sectors().stream()
                .map(SectorWeights.Row::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> problems = new ArrayList<>();

        // ID set equality
        if (!idsCat.equals(idsGat) || !idsCat.equals(idsWei)) {
            problems.add("Sector ID sets differ across files. " +
                    "catalog=" + idsCat.size() + ", gates=" + idsGat.size() + ", weights=" + idsWei.size());
            problems.add("Missing in gates: " + diff(idsCat, idsGat));
            problems.add("Missing in weights: " + diff(idsCat, idsWei));
        }

        // Row-sum + key checks
        List<String> rowIssues = new ArrayList<>();
        for (SectorWeights.Row r : w.sectors()) {
            double sum = safeSum(r.weights());
            if (Math.abs(sum - 1.0) > rowTolerance) {
                rowIssues.add(r.id() + " sum=" + sum);
            }
            if (!validTraitKeys(r.weights().keySet())) {
                rowIssues.add(r.id() + " has invalid trait keys: " + r.weights().keySet());
            }
        }
        if (!rowIssues.isEmpty()) {
            problems.add("Row-sum or key issues: " + rowIssues);
        }

        return new ValidationReport(idsCat.size(), problems);
    }

    private static Set<String> diff(Set<String> a, Set<String> b) {
        Set<String> d = new LinkedHashSet<>(a);
        d.removeAll(b);
        return d;
    }

    private static double safeSum(Map<String, Double> m) {
        if (m == null || m.isEmpty()) return 0.0;
        return m.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private static boolean validTraitKeys(Set<String> keys) {
        if (keys == null || keys.size() != 12) return false;
        for (String k : keys) {
            if (!k.matches("T\\d{2}")) return false;
            int n = Integer.parseInt(k.substring(1));
            if (n < 1 || n > Trait.values().length) return false;
        }
        return true;
    }

    public record ValidationReport(int sectorCount, List<String> problems) {
        public boolean ok() { return problems == null || problems.isEmpty(); }
    }
}