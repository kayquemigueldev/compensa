package com.kayque.compensa.alerts.model;

import java.util.Objects;

public record SmartAlert(
        String code,
        SmartAlertTopic topic,
        SmartAlertPriority priority,
        String title,
        String message
) {

    public SmartAlert {
        code = requireText(
                code,
                "O código do alerta é obrigatório."
        );

        Objects.requireNonNull(
                topic,
                "O assunto do alerta é obrigatório."
        );

        Objects.requireNonNull(
                priority,
                "A prioridade do alerta é obrigatória."
        );

        title = requireText(
                title,
                "O título do alerta é obrigatório."
        );

        message = requireText(
                message,
                "A mensagem do alerta é obrigatória."
        );
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}