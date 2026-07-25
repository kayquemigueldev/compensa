package com.kayque.compensa.dashboard.model;

import com.kayque.compensa.navigation.NavigationTarget;

import java.util.Objects;

public record DashboardSmartAlertView(
        String title,
        String message,
        String styleClass,
        NavigationTarget navigationTarget
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

        Objects.requireNonNull(
                navigationTarget,
                "O destino do alerta é obrigatório."
        );
    }

    public DashboardSmartAlertView(
            String title,
            String message,
            String styleClass
    ) {
        this(
                title,
                message,
                styleClass,
                NavigationTarget.NONE
        );
    }

    public boolean hasNavigation() {
        return navigationTarget != NavigationTarget.NONE;
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