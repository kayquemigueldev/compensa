package com.kayque.compensa.alerts.model;

public record SmartAlertFilterSummary(
        int total,
        int unread,
        int read
) {

    public SmartAlertFilterSummary {
        if (total < 0 || unread < 0 || read < 0) {
            throw new IllegalArgumentException(
                    "As contagens dos alertas não podem ser negativas."
            );
        }

        if (unread + read != total) {
            throw new IllegalArgumentException(
                    "A soma dos alertas novos e lidos deve corresponder ao total."
            );
        }
    }
}