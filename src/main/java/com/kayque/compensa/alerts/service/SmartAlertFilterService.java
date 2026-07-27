package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertFilter;
import com.kayque.compensa.alerts.model.SmartAlertFilterSummary;

import java.util.List;
import java.util.Objects;

public class SmartAlertFilterService {

    private final SmartAlertReadService readService;

    public SmartAlertFilterService(
            SmartAlertReadService readService
    ) {
        this.readService = Objects.requireNonNull(
                readService,
                "O serviço de leitura dos alertas é obrigatório."
        );
    }

    public List<SmartAlert> filter(
            List<SmartAlert> alerts,
            SmartAlertFilter filter
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        Objects.requireNonNull(
                filter,
                "O filtro dos alertas é obrigatório."
        );

        readService.synchronize(alerts);

        return alerts.stream()
                .filter(alert ->
                        matchesFilter(alert, filter)
                )
                .toList();
    }

    public SmartAlertFilterSummary summarize(
            List<SmartAlert> alerts
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        readService.synchronize(alerts);

        int read = (int) alerts.stream()
                .filter(readService::isRead)
                .count();

        int total = alerts.size();
        int unread = total - read;

        return new SmartAlertFilterSummary(
                total,
                unread,
                read
        );
    }

    private boolean matchesFilter(
            SmartAlert alert,
            SmartAlertFilter filter
    ) {
        return switch (filter) {
            case ALL -> true;
            case UNREAD -> !readService.isRead(alert);
            case READ -> readService.isRead(alert);
        };
    }
}