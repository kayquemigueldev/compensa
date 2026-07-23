package com.kayque.compensa.dashboard.model;

public record DashboardSmartAlertView(
        String title,
        String message,
        String styleClass
) {

    public DashboardSmartAlertView {
        title = requireText(
                title,
                "O título do alerta é obrigatório."
        );

        message = requireText(
                message,
                "A mensagem do alerta é obrigatória."
        );

        styleClass = requireText(
                styleClass,
                "O estilo do alerta é obrigatório."
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