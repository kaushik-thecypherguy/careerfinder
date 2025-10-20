package com.acf.careerfinder.sector.model1;

import java.util.List;

/**
 * Gate rule fields mirror your Cand_* variables (Q1..Q24 mapping).
 * Any field = null => Not Required (N/A) for that sector.
 */
public record SectorGates(List<SectorGate> sectors) {

    public record SectorGate(String id, Requirements req) {}

    public record Requirements(
            Integer minEdu,              // Cand_Edu (0..6)
            Integer minAge,              // years
            Integer minHeightCm,         // Cand_HeightCm lower bound
            Integer minLiftKg,           // Cand_LiftKg lower bound
            Integer maxCommuteKm,        // Cand_CommuteKm upper bound user is OK with

            Boolean needsStandingOk,     // Cand_Standing_OK
            Boolean needsNightOk,        // Cand_Night_OK
            Boolean needsWeekendOk,      // Cand_Weekend_OK
            Boolean needsFieldTravelOk,  // Cand_Field_Travel_OK
            Boolean needsWorkAtHeightOk, // Cand_WorkAtHeight_OK

            Boolean needsSmartphone,     // Cand_Smartphone
            Boolean needsDocs,           // Cand_HasDocs
            Boolean needsDL,             // Cand_HasDL
            Boolean needs2W,             // Cand_Has2W
            Boolean needsPSARA,          // Cand_HasPSARA
            Boolean needsAEP,            // Cand_HasAEP
            Boolean needsBGC,            // Cand_BGC

            Boolean needsEnglishBasic,   // Cand_EnglishBasic
            Boolean needsLocalLanguage,  // Cand_LocalLanguage
            Boolean needsComputerBasics, // Cand_Computer_Basics
            Boolean needsNormalVision,   // Cand_NormalVision
            Boolean needsColorVisionOk,  // Cand_ColorVision_OK
            Boolean needsVaccProof,      // Cand_VaccProof

            Integer minTypingWPM         // Cand_Typing_WPM
    ) {}
}