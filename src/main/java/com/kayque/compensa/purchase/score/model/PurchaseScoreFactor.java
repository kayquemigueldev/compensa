package com.kayque.compensa.purchase.score.model;

import java.util.Objects;

public record PurchaseScoreFactor(
        String description,
        int points
) {

    public PurchaseScoreFactor {
        Objects.requireNonNull(
                description,
                "A descrição do fator é obrigatória."
        );

        description = description.trim();

        if (description.isEmpty()) {
            throw new IllegalArgumentException(
                    "A descrição do fator não pode estar vazia."
            );
        }

        if (points == 0) {
            throw new IllegalArgumentException(
                    "O impacto do fator não pode ser zero."
            );
        }
    }

    public boolean isPositive() {
        return points > 0;
    }

    public boolean isNegative() {
        return points < 0;
    }
}