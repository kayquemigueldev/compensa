package com.kayque.compensa.goal.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public record SavingsGoalForecast(
        SavingsGoalForecastStatus status,
        String message,
        LocalDate estimatedCompletionDate
) {

    public SavingsGoalForecast {
        Objects.requireNonNull(
                status,
                "O status da previsão é obrigatório."
        );

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "A mensagem da previsão é obrigatória."
            );
        }
    }

    public Optional<LocalDate> completionDate() {
        return Optional.ofNullable(
                estimatedCompletionDate
        );
    }
}