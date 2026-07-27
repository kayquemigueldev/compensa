package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.repository.SmartAlertReadRepository;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartAlertReadService {

    private final SmartAlertReadRepository repository;
    private final Clock clock;

    public SmartAlertReadService(
            SmartAlertReadRepository repository,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório de alertas lidos é obrigatório."
        );

        this.clock = Objects.requireNonNull(
                clock,
                "O relógio é obrigatório."
        );
    }

    public void markAsRead(SmartAlert alert) {
        Objects.requireNonNull(
                alert,
                "O alerta é obrigatório."
        );

        repository.markAsRead(
                alert.code(),
                clock.instant()
        );
    }

    public boolean isRead(SmartAlert alert) {
        Objects.requireNonNull(
                alert,
                "O alerta é obrigatório."
        );

        return repository.isRead(alert.code());
    }

    public List<SmartAlert> filterUnread(
            List<SmartAlert> alerts
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        synchronize(alerts);

        return alerts.stream()
                .filter(alert ->
                        !repository.isRead(alert.code())
                )
                .toList();
    }

    public int countUnread(List<SmartAlert> alerts) {
        return filterUnread(alerts).size();
    }

    public void synchronize(List<SmartAlert> activeAlerts) {
        Objects.requireNonNull(
                activeAlerts,
                "A lista de alertas ativos é obrigatória."
        );

        Set<String> activeCodes =
                activeAlerts.stream()
                        .map(SmartAlert::code)
                        .collect(Collectors.toSet());

        repository.findAllReadCodes()
                .stream()
                .filter(code ->
                        !activeCodes.contains(code)
                )
                .forEach(repository::delete);
    }
}