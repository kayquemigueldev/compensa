package com.kayque.compensa.alerts.repository;

import java.time.Instant;

public interface SmartAlertSnoozeRepository {

    void save(
            String alertCode,
            Instant snoozedUntil
    );

    boolean isSnoozed(
            String alertCode,
            Instant currentInstant
    );

    void deleteExpired(Instant currentInstant);
}