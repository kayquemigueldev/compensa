package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;

import java.util.List;
import java.util.Objects;

public class DashboardSmartAlertPresentationService {

    public List<DashboardSmartAlertView> prepare(
            List<SmartAlert> alerts,
            int limit
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "O limite de alertas deve ser positivo."
            );
        }

        if (alerts.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "A lista de alertas não pode conter valores nulos."
            );
        }

        return alerts.stream()
                .limit(limit)
                .map(this::createView)
                .toList();
    }

    private DashboardSmartAlertView createView(
            SmartAlert alert
    ) {
        return new DashboardSmartAlertView(
                alert.title(),
                alert.message(),
                getStyleClass(alert.priority())
        );
    }

    private String getStyleClass(
            SmartAlertPriority priority
    ) {
        return switch (priority) {
            case INFORMATIONAL ->
                    "dashboard-smart-alert-informational";

            case ATTENTION ->
                    "dashboard-smart-alert-attention";

            case CRITICAL ->
                    "dashboard-smart-alert-critical";
        };
    }
}