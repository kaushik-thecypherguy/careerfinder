package com.acf.careerfinder.geo;

import java.util.*;

/**
 * Maharashtra districts (canonical display names) + a few common aliases.
 * Used to populate the gating dropdown and to normalize whatever is posted back.
 */
public final class MHLocation {

    private MHLocation() {}

    /** Canonical display names (alphabetical). */
    private static final List<String> DISTRICTS = List.of(
            "Ahmednagar",
            "Akola",
            "Amravati",
            "Bhandara",
            "Beed",
            "Buldhana",
            "Chandrapur",
            "Chhatrapati Sambhajinagar (Aurangabad)",
            "Dhule",
            "Dharashiv (Osmanabad)",
            "Gadchiroli",
            "Gondia",
            "Hingoli",
            "Jalgaon",
            "Jalna",
            "Kolhapur",
            "Latur",
            "Mumbai City",
            "Mumbai Suburban",
            "Nagpur",
            "Nanded",
            "Nandurbar",
            "Nashik",
            "Palghar",
            "Parbhani",
            "Pune",
            "Raigad",
            "Ratnagiri",
            "Sangli",
            "Satara",
            "Sindhudurg",
            "Solapur",
            "Thane",
            "Wardha",
            "Washim",
            "Yavatmal"
    );

    /** Aliases (lowercase) → canonical display. */
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("aurangabad", "Chhatrapati Sambhajinagar (Aurangabad)"),
            Map.entry("chhatrapati sambhajinagar", "Chhatrapati Sambhajinagar (Aurangabad)"),
            Map.entry("c. sambhajinagar", "Chhatrapati Sambhajinagar (Aurangabad)"),
            Map.entry("sambhajinagar", "Chhatrapati Sambhajinagar (Aurangabad)"),
            Map.entry("osmanabad", "Dharashiv (Osmanabad)"),
            Map.entry("dharashiv", "Dharashiv (Osmanabad)"),
            Map.entry("gondiya", "Gondia"),
            Map.entry("beed (bhir)", "Beed"),
            Map.entry("bhir", "Beed"),
            Map.entry("mumbai suburban", "Mumbai Suburban"),
            Map.entry("mumbai city", "Mumbai City")
            // (Add more spellings if you see them in the wild)
    );

    /** Unmodifiable canonical list for dropdowns. */
    public static List<String> districts() {
        return Collections.unmodifiableList(DISTRICTS);
    }

    /** True if name matches any canonical or alias (case‑insensitive). */
    public static boolean isValidDistrict(String name) {
        if (name == null) return false;
        String n = name.trim();
        if (n.isEmpty()) return false;

        for (String d : DISTRICTS) {
            if (d.equalsIgnoreCase(n)) return true;
        }
        String key = n.toLowerCase(Locale.ROOT);
        return ALIASES.containsKey(key);
    }

    /**
     * Returns the canonical display string if matched (case-insensitive),
     * falls back to a normalized alias match, else returns the trimmed input.
     */
    public static String canonicalize(String name) {
        if (name == null) return null;
        String n = name.trim();
        if (n.isEmpty()) return null;

        for (String d : DISTRICTS) {
            if (d.equalsIgnoreCase(n)) return d;
        }
        String key = n.toLowerCase(Locale.ROOT);
        String alias = ALIASES.get(key);
        return alias != null ? alias : n;
    }
}