package com.kayque.compensa.goal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record SavingsGoalAchievement(
        long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        LocalDate targetDate,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public SavingsGoalAchievement {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O identificador da conquista deve ser positivo."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da conquista é obrigatório."
            );
        }

        name = name.trim();

        if (name.length() > 120) {
            throw new IllegalArgumentException(
                    "O nome da conquista deve ter no máximo 120 caracteres."
            );
        }

        Objects.requireNonNull(
                targetAmount,
                "O valor planejado é obrigatório."
        );

        Objects.requireNonNull(
                savedAmount,
                "O valor guardado é obrigatório."
        );

        Objects.requireNonNull(
                createdAt,
                "A data de criação é obrigatória."
        );

        Objects.requireNonNull(
                completedAt,
                "A data de conclusão é obrigatória."
        );

        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor planejado deve ser maior que zero."
            );
        }

        if (savedAmount.compareTo(targetAmount) < 0) {
            throw new IllegalArgumentException(
                    "Uma conquista concluída deve atingir o valor planejado."
            );
        }

        if (completedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "A conclusão não pode acontecer antes da criação."
            );
        }
    }

    public BigDecimal exceededAmount() {
        return savedAmount
                .subtract(targetAmount)
                .max(BigDecimal.ZERO);
    }

    public boolean exceededTarget() {
        return exceededAmount()
                .compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hadTargetDate() {
        return targetDate != null;
    }
}