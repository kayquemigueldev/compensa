package com.kayque.compensa.goal.model;

import java.math.BigDecimal;

public record SavingsGoal(
        String name,
        BigDecimal targetAmount,
        BigDecimal savedAmount
) {

    public SavingsGoal {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o nome do objetivo."
            );
        }

        name = name.trim();

        if (name.length() > 120) {
            throw new IllegalArgumentException(
                    "O objetivo deve possuir no máximo 120 caracteres."
            );
        }

        if (targetAmount == null
                || targetAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new IllegalArgumentException(
                    "O valor do objetivo deve ser maior que zero."
            );
        }

        savedAmount = savedAmount == null
                ? BigDecimal.ZERO
                : savedAmount;

        if (savedAmount.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "O valor guardado não pode ser negativo."
            );
        }
    }

    public BigDecimal remainingAmount() {
        BigDecimal remaining =
                targetAmount.subtract(savedAmount);

        return remaining.max(BigDecimal.ZERO);
    }

    public boolean isCompleted() {
        return savedAmount.compareTo(targetAmount) >= 0;
    }
}