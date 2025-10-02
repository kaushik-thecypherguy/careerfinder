package com.acf.careerfinder.psychometrics;

/** SJT item formats supported in scoring. */
public enum SjtFormat {
    MULTI_SELECT,   // multiple options can be marked; scored via E/O/X normalization
    YES_NO,         // discrete; correct = 4, incorrect = 0
    SINGLE_BEST     // MCQ single-best; correct = 4, incorrect = 0
}