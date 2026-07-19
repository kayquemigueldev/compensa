package com.kayque.compensa.goal.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public enum SavingsGoalMilestone {

    TWENTY_FIVE(
            25,
            "25% alcançados! Sua conquista já começou a tomar forma."
    ),

    FIFTY(
            50,
            "Metade do caminho! Você já conquistou 50% do objetivo."
    ),

    SEVENTY_FIVE(
            75,
            "75% alcançados! Falta pouco para transformar o objetivo em realidade."
    ),

    COMPLETED(
            100,
            "Objetivo alcançado! Você chegou ao valor planejado."
    );

    private final int percentage;
    private final String message;

    SavingsGoalMilestone(
            int percentage,
            String message
    ) {
        this.percentage = percentage;
        this.message = message;
    }

    public int percentage() {
        return percentage;
    }

    public String message() {
        return message;
    }

    public static Optional<SavingsGoalMilestone>
    highestReached(int percentage) {
        return Arrays.stream(values())
                .filter(milestone ->
                        percentage >= milestone.percentage
                )
                .max(
                        Comparator.comparingInt(
                                SavingsGoalMilestone::percentage
                        )
                );
    }
}