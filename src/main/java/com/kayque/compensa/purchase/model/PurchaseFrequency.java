package com.kayque.compensa.purchase.model;

public enum PurchaseFrequency {

    ONCE(1),
    MONTHLY(12),
    WEEKLY(52),
    DAILY(365);

    private final int yearlyOccurrences;

    PurchaseFrequency(int yearlyOccurrences) {
        this.yearlyOccurrences = yearlyOccurrences;
    }

    public int yearlyOccurrences() {
        return yearlyOccurrences;
    }
}