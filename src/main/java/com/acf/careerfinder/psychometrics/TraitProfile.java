package com.acf.careerfinder.psychometrics;

import java.util.EnumMap;
import java.util.Map;

/** In‑memory score container passed to the result page. */
public final class TraitProfile {

    private final EnumMap<Domain, Integer> ipipRaw = new EnumMap<>(Domain.class);
    private final EnumMap<Domain, Double>  ocean0to100 = new EnumMap<>(Domain.class);
    private final EnumMap<Trait, Double>   traitFromIpip0to100 = new EnumMap<>(Trait.class);
    private final EnumMap<Trait, Double>   traitSjt0to100 = new EnumMap<>(Trait.class);
    private final EnumMap<Trait, Double>   traitFinal0to100 = new EnumMap<>(Trait.class);

    // New topline metrics (optional to display, useful to have)
    private double ipipOverall0to100;
    private double sjtOverall0to100;
    private double composite0to100;

    public TraitProfile() {
        for (Domain d : Domain.values()) {
            ipipRaw.put(d, 0);
            ocean0to100.put(d, 0.0);
        }
        for (Trait t : Trait.values()) {
            traitFromIpip0to100.put(t, 0.0);
            traitSjt0to100.put(t, 0.0);
            traitFinal0to100.put(t, 0.0);
        }
    }

    // ---- record-like accessors ----
    public Map<Domain, Integer> ipipRaw()             { return ipipRaw; }
    public Map<Domain, Double>  ocean0to100()         { return ocean0to100; }
    public Map<Trait, Double>   traitFromIpip0to100() { return traitFromIpip0to100; }
    public Map<Trait, Double>   traitSjt0to100()      { return traitSjt0to100; }
    public Map<Trait, Double>   traitFinal0to100()    { return traitFinal0to100; }

    // ---- classic getters (unchanged) ----
    public Map<Domain, Integer> getIpipRaw()             { return ipipRaw; }
    public Map<Domain, Double>  getOcean0to100()         { return ocean0to100; }
    public Map<Trait, Double>   getTraitFromIpip0to100() { return traitFromIpip0to100; }
    public Map<Trait, Double>   getTraitSjt0to100()      { return traitSjt0to100; }
    public Map<Trait, Double>   getTraitFinal0to100()    { return traitFinal0to100; }

    // ---- new getters/setters for toplines ----
    public double getIpipOverall0to100() { return ipipOverall0to100; }
    public void setIpipOverall0to100(double v) { this.ipipOverall0to100 = v; }

    public double getSjtOverall0to100() { return sjtOverall0to100; }
    public void setSjtOverall0to100(double v) { this.sjtOverall0to100 = v; }

    public double getComposite0to100() { return composite0to100; }
    public void setComposite0to100(double v) { this.composite0to100 = v; }

    @Override public String toString() {
        return "TraitProfile{" +
                "ipipRaw=" + ipipRaw +
                ", ocean0to100=" + ocean0to100 +
                ", traitFromIpip0to100=" + traitFromIpip0to100 +
                ", traitSjt0to100=" + traitSjt0to100 +
                ", traitFinal0to100=" + traitFinal0to100 +
                ", ipipOverall0to100=" + ipipOverall0to100 +
                ", sjtOverall0to100=" + sjtOverall0to100 +
                ", composite0to100=" + composite0to100 +
                '}';
    }
}