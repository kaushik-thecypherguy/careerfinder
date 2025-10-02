package com.acf.careerfinder.psychometrics;

import java.util.EnumMap;
import java.util.Map;

import static com.acf.careerfinder.psychometrics.Domain.*;
import static com.acf.careerfinder.psychometrics.Trait.*;

/**
 * Immutable holder object for computed scores.
 * We expose both record-like accessors (mapName()) and classic getters (getMapName())
 * so callers can use either style.
 */
public final class TraitProfile {

    private final EnumMap<Domain, Integer> ipipRaw = new EnumMap<>(Domain.class);
    private final EnumMap<Domain, Double>  ocean0to100 = new EnumMap<>(Domain.class);
    private final EnumMap<Trait, Double>   traitFromIpip0to100 = new EnumMap<>(Trait.class);
    private final EnumMap<Trait, Double>   traitSjt0to100 = new EnumMap<>(Trait.class);
    private final EnumMap<Trait, Double>   traitFinal0to100 = new EnumMap<>(Trait.class);

    // ---- record-like accessors (what ScoringService uses) ----
    public Map<Domain, Integer> ipipRaw()            { return ipipRaw; }
    public Map<Domain, Double>  ocean0to100()        { return ocean0to100; }
    public Map<Trait, Double>   traitFromIpip0to100(){ return traitFromIpip0to100; }
    public Map<Trait, Double>   traitSjt0to100()     { return traitSjt0to100; }
    public Map<Trait, Double>   traitFinal0to100()   { return traitFinal0to100; }

    // ---- classic getters (keep both styles available) ----
    public Map<Domain, Integer> getIpipRaw()             { return ipipRaw; }
    public Map<Domain, Double>  getOcean0to100()         { return ocean0to100; }
    public Map<Trait, Double>   getTraitFromIpip0to100() { return traitFromIpip0to100; }
    public Map<Trait, Double>   getTraitSjt0to100()      { return traitSjt0to100; }
    public Map<Trait, Double>   getTraitFinal0to100()    { return traitFinal0to100; }

    @Override public String toString() {
        return "TraitProfile{" +
                "ipipRaw=" + ipipRaw +
                ", ocean0to100=" + ocean0to100 +
                ", traitFromIpip0to100=" + traitFromIpip0to100 +
                ", traitSjt0to100=" + traitSjt0to100 +
                ", traitFinal0to100=" + traitFinal0to100 +
                '}';
    }
}