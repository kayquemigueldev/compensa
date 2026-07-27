package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
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
            SmartAlertSnoozeRepository repository
    ) {
        this(
                repository,
                Clock.systemDefaultZone()
        );
    }

    public SmartAlertSnoozeService(
            SmartAlertSnoozeRepository repository,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório de adiamentos é obrigatório."
        );

        this.clock = Objects.requireNonNull(
                clock,
                "O relógio da aplicação é obrigatório."
        );
    }

    public void snooze(
            SmartAlert alert,
            Duration duration
    ) {
        Objects.requireNonNull(
                alert,
                "O alerta é obrigatório."
        );

        snooze(
                alert.code(),
                duration
        );
    }

    public void snooze(
            String alertCode,
            Duration duration
    ) {
        if (alertCode == null || alertCode.isBlank()) {
            throw new IllegalArgumentException(
                    "O código do alerta é obrigatório."
            );
        }

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
                alertCode.trim(),
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
}