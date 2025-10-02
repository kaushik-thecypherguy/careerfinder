package com.acf.careerfinder.psychometrics;

/**
 * Our 12 work-style traits (names align with your workbook).
 * Keep these IDs stable; labels can be localized in the UI later.
 */
public enum Trait {
    T01_SAFETY("Safety"),
    T02_TEAMWORK("Teamwork"),
    T03_COMMUNICATION("Communication"),
    T04_SERVICE("Service Orientation"),
    T05_PRO_DEMEANOUR("Professional Demeanour"),
    T06_COMPLIANCE("Compliance / Policy Adherence"),
    T07_DOCUMENTATION("Documentation & Record Keeping"),
    T08_ATTENTION("Attention to Detail"),
    T09_PLANNING("Planning & Organizing"),
    T10_ADAPTABILITY("Adaptability / Learning"),
    T11_DEESCALATION("De-escalation / Self-control"),
    T12_INCLUSIVITY("Inclusivity / Respect");

    private final String label;
    Trait(String label) { this.label = label; }
    public String label() { return label; }
}