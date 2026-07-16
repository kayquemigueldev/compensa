package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SavingsGoalProgress(
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        BigDecimal remainingAmount,
        BigDecimal percentage,
        SavingsGoalProgressStatus status
) {

    public SavingsGoalProgress {
        Objects.requireNonNull(
                targetAmount,
                "O valor total do objetivo é obrigatório."
        );

        Objects.requireNonNull(
                savedAmount,
                "O valor guardado é obrigatório."
        );

        Objects.requireNonNull(
                remainingAmount,
                "O valor restante é obrigatório."
        );

        Objects.requireNonNull(
                percentage,
                "A porcentagem do objetivo é obrigatória."
        );

        Objects.requireNonNull(
                status,
                "O status do objetivo é obrigatório."
        );
    }
}