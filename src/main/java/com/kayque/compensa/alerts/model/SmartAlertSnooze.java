package com.kayque.compensa.alerts.model;

import java.time.Instant;
import java.util.Objects;

public record SmartAlertSnooze(
        String alertCode,
        Instant snoozedUntil
) {

    public SmartAlertSnooze {
        if (alertCode == null || alertCode.isBlank()) {
            throw new IllegalArgumentException(
                    "O código do alerta é obrigatório."
            );
        }

        alertCode = alertCode.trim();

        Objects.requireNonNull(
                snoozedUntil,
                "A data final do adiamento é obrigatória."
        );
    }

    public boolean isActiveAt(Instant currentInstant) {
        Objects.requireNonNull(
                currentInstant,
                "O instante atual é obrigatório."
        );

        return snoozedUntil.isAfter(currentInstant);
    }
}