package com.acf.careerfinder.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordValidator {
    private static final int MIN_LEN = 7;
    private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern HAS_DIGIT  = Pattern.compile("\\d");

    public void validateOrThrow(String pw) {
        if (pw == null || pw.isBlank())
            throw new IllegalArgumentException("Password is required.");
        if (pw.length() < MIN_LEN)
            throw new IllegalArgumentException("Password must be at least " + MIN_LEN + " characters.");
        if (!HAS_LETTER.matcher(pw).find())
            throw new IllegalArgumentException("Password must contain at least one letter.");
        if (!HAS_DIGIT.matcher(pw).find())
            throw new IllegalArgumentException("Password must contain at least one number.");
        // reject single repeated character like "aaaaaaa" or "1111111"
        long distinct = pw.chars().distinct().count();
        if (distinct == 1)
            throw new IllegalArgumentException("Password cannot be a single character repeated.");
    }
}


