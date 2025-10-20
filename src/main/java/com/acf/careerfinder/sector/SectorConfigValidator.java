package com.acf.careerfinder.sector;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Runs once at startup; fails fast if config is inconsistent. */
@Component
public class SectorConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(SectorConfigValidator.class);

    private final SectorConfigService cfg;

    public SectorConfigValidator(SectorConfigService cfg) { this.cfg = cfg; }

    @PostConstruct
    public void validate() {
        var rep = cfg.validateAll();
        if (rep.ok()) {
            log.info("Sector config OK: {} sectors; gates & weights aligned; row-sums within tolerance.",
                    rep.sectorCount());
        } else {
            log.error("Sector config problems: {}", rep.problems());
            throw new IllegalStateException("Sector config invalid. See logs for details.");
        }
    }
}