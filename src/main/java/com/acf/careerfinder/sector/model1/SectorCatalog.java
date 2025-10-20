package com.acf.careerfinder.sector.model1;

import java.util.List;

/** Minimal catalog: we only care about id + display name. */
public record SectorCatalog(List<Sector> sectors) {
    public record Sector(String id, String name) {}
}