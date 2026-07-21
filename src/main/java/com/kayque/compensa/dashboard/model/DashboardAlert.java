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
                    "O título da notificação é obrigatório."
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "A mensagem da notificação é obrigatória."
            );
        }

        Objects.requireNonNull(
                level,
                "O nível da notificação é obrigatório."
        );
    }
}