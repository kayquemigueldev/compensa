package com.kayque.compensa.dashboard.model;

import java.util.Objects;

public record DashboardAlert(
        String title,
        String message,
        DashboardAlertLevel level
) {

    public DashboardAlert {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "O título do alerta é obrigatório."
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "A mensagem do alerta é obrigatória."
            );
        }

        Objects.requireNonNull(
                level,
                "O nível do alerta é obrigatório."
        );

        title = title.trim();
        message = message.trim();
    }
}