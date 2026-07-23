package com.kayque.compensa.alerts.model;

public enum SmartAlertPriority {

    INFORMATIONAL(1),
    ATTENTION(2),
    CRITICAL(3);

    private final int weight;

    SmartAlertPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    public boolean isHigherThan(
            SmartAlertPriority other
    ) {
        if (other == null) {
            return true;
        }

        return weight > other.weight;
    }
}