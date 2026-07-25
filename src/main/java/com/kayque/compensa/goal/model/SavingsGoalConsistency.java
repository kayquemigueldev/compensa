package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SavingsGoalConsistency(
        BigDecimal contributedThisMonth,
        int contributionsThisMonth,
        int consecutiveMonths,
        SavingsGoalConsistencyStatus status
) {

    public SavingsGoalConsistency {
        Objects.requireNonNull(
                contributedThisMonth,
                "O valor contribuído no mês é obrigatório."
        );

        Objects.requireNonNull(
                status,
                "O status da consistência é obrigatório."
        );

        if (contributedThisMonth.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor contribuído no mês não pode ser negativo."
            );
        }

        if (contributionsThisMonth < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de contribuições não pode ser negativa."
            );
        }

        if (consecutiveMonths < 0) {
            throw new IllegalArgumentException(
                    "A sequência de meses não pode ser negativa."
            );
        }
    }
}