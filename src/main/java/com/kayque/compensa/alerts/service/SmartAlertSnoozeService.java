package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertSnooze;
import com.kayque.compensa.alerts.repository.SmartAlertSnoozeRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class SmartAlertSnoozeService {

    private final SmartAlertSnoozeRepository repository;
    private final Clock clock;

    public SmartAlertSnoozeService(
            SmartAlertSnoozeRepository repository,
            Clock clock
    ) {
        this.repository =
                Objects.requireNonNull(repository);

        this.clock = Objects.requireNonNull(clock);
    }

    public void snooze(
            SmartAlert alert,
            Duration duration
    ) {
        Objects.requireNonNull(
                alert,
                "O alerta é obrigatório."
        );

        snooze(alert.code(), duration);
    }

    public void snooze(
            String alertCode,
            Duration duration
    ) {
        Objects.requireNonNull(
                duration,
                "A duração do adiamento é obrigatória."
        );

        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "A duração do adiamento deve ser positiva."
            );
        }

        Instant snoozedUntil =
                clock.instant().plus(duration);

        repository.save(
                alertCode,
                snoozedUntil
        );
    }

    public List<SmartAlert> filterVisible(
            List<SmartAlert> alerts
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        Instant currentInstant = clock.instant();

        repository.deleteExpired(currentInstant);

        return alerts.stream()
                .filter(alert ->
                        !repository.isSnoozed(
                                alert.code(),
                                currentInstant
                        )
                )
                .toList();
    }

    public List<SmartAlertSnooze> findActive() {
        Instant currentInstant = clock.instant();

        repository.deleteExpired(currentInstant);

        return repository.findActive(currentInstant);
    }

    public void restore(String alertCode) {
        repository.delete(alertCode);
    }

    public void restoreAll() {
        repository.deleteAll();
    }
}