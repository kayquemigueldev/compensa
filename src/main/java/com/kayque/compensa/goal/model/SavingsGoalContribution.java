package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record SavingsGoalContribution(
        long id,
        BigDecimal amount,
        LocalDateTime contributedAt
) {

    public SavingsGoalContribution {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O ID da contribuição deve ser positivo."
            );
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor da contribuição deve ser maior que zero."
            );
        }

        Objects.requireNonNull(
                contributedAt,
                "A data da contribuição é obrigatória."
        );
    }
}