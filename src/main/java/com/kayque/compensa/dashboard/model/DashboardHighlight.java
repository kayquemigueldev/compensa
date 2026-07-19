package com.kayque.compensa.dashboard.model;

import java.util.Objects;

public record DashboardHighlight(
        String title,
        String description,
        DashboardHighlightType type
) {

    public DashboardHighlight {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "O título do destaque é obrigatório."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do destaque é obrigatória."
            );
        }

        Objects.requireNonNull(
                type,
                "O tipo do destaque é obrigatório."
        );

        title = title.trim();
        description = description.trim();
    }
}