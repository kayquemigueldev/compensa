package com.kayque.compensa.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record DashboardWeeklyOverview(
        LocalDate weekStart,
        LocalDate weekEnd,
        DashboardWeeklySummary currentWeek,
        DashboardWeeklySummary previousWeek,
        DashboardWeeklySpendingTrend spendingTrend,
        BigDecimal spendingVariationPercentage
) {

    public DashboardWeeklyOverview {
        Objects.requireNonNull(
                weekStart,
                "O início da semana é obrigatório."
        );

        Objects.requireNonNull(
                weekEnd,
                "O fim da semana é obrigatório."
        );

        Objects.requireNonNull(
                currentWeek,
                "O resumo da semana atual é obrigatório."
        );

        Objects.requireNonNull(
                previousWeek,
                "O resumo da semana anterior é obrigatório."
        );

        Objects.requireNonNull(
                spendingTrend,
                "A tendência semanal é obrigatória."
        );

        Objects.requireNonNull(
                spendingVariationPercentage,
                "A variação semanal é obrigatória."
        );

        if (weekStart.isAfter(weekEnd)) {
            throw new IllegalArgumentException(
                    "O início da semana não pode ser posterior ao fim."
            );
        }

        if (spendingVariationPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "A variação semanal não pode ser negativa."
            );
        }
    }
}