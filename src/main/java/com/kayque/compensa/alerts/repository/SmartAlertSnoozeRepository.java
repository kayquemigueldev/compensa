package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.alerts.model.SmartAlertSnooze;

import java.time.Instant;
import java.util.List;

public interface SmartAlertSnoozeRepository {

    void save(
            String alertCode,
            Instant snoozedUntil
    );

    boolean isSnoozed(
            String alertCode,
            Instant currentInstant
    );

    List<SmartAlertSnooze> findActive(
            Instant currentInstant
    );

    void delete(String alertCode);

    void deleteAll();

    void deleteExpired(Instant currentInstant);
}