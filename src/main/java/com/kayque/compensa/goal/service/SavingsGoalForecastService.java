package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SavingsGoalForecastService {

    private static final BigDecimal DAYS_PER_MONTH =
            new BigDecimal("30");

    public SavingsGoalForecast calculate(
            SavingsGoal goal,
            List<SavingsGoalContribution> contributions,
            LocalDate currentDate
    ) {
        Objects.requireNonNull(
                goal,
                "O objetivo é obrigatório."
        );

        Objects.requireNonNull(
                contributions,
                "As contribuições são obrigatórias."
        );

        Objects.requireNonNull(
                currentDate,
                "A data atual é obrigatória."
        );

        if (goal.isCompleted()) {
            return new SavingsGoalForecast(
                    SavingsGoalForecastStatus.COMPLETED,
                    "Objetivo concluído. Você alcançou o valor planejado!",
                    currentDate
            );
        }

        if (contributions.isEmpty()) {
            return new SavingsGoalForecast(
                    SavingsGoalForecastStatus.NO_HISTORY,
                    "Registre contribuições para gerar uma previsão de conclusão.",
                    null
            );
        }

        if (contributions.size() == 1) {
            return createContributionCountForecast(
                    goal,
                    contributions.getFirst().amount()
            );
        }

        List<SavingsGoalContribution> ordered =
                contributions.stream()
                        .sorted(
                                Comparator.comparing(
                                        SavingsGoalContribution::contributedAt
                                )
                        )
                        .toList();

        LocalDate firstDate = ordered.getFirst()
                .contributedAt()
                .toLocalDate();

        LocalDate lastDate = ordered.getLast()
                .contributedAt()
                .toLocalDate();

        long observedDays = ChronoUnit.DAYS.between(
                firstDate,
                lastDate
        );

        BigDecimal totalContributed = ordered.stream()
                .map(SavingsGoalContribution::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        if (observedDays <= 0) {
            BigDecimal averageContribution =
                    totalContributed.divide(
                            BigDecimal.valueOf(
                                    ordered.size()
                            ),
                            2,
                            RoundingMode.HALF_UP
                    );

            return createContributionCountForecast(
                    goal,
                    averageContribution
            );
        }

        BigDecimal dailyAverage =
                totalContributed.divide(
                        BigDecimal.valueOf(observedDays),
                        8,
                        RoundingMode.HALF_UP
                );

        if (dailyAverage.compareTo(BigDecimal.ZERO) <= 0) {
            return new SavingsGoalForecast(
                    SavingsGoalForecastStatus.NO_HISTORY,
                    "Ainda não existem dados suficientes para estimar uma data.",
                    null
            );
        }

        long estimatedDays = goal.remainingAmount()
                .divide(
                        dailyAverage,
                        0,
                        RoundingMode.CEILING
                )
                .longValue();

        LocalDate estimatedDate =
                currentDate.plusDays(estimatedDays);

        return new SavingsGoalForecast(
                SavingsGoalForecastStatus.ESTIMATED_DATE,
                "Mantendo o ritmo atual, sua conquista poderá acontecer por volta de",
                estimatedDate
        );
    }

    private SavingsGoalForecast
    createContributionCountForecast(
            SavingsGoal goal,
            BigDecimal referenceAmount
    ) {
        long contributionsNeeded =
                goal.remainingAmount()
                        .divide(
                                referenceAmount,
                                0,
                                RoundingMode.CEILING
                        )
                        .longValue();

        String contributionWord =
                contributionsNeeded == 1
                        ? "contribuição semelhante"
                        : "contribuições semelhantes";

        return new SavingsGoalForecast(
                SavingsGoalForecastStatus.CONTRIBUTIONS_NEEDED,
                "Faltam cerca de "
                        + contributionsNeeded
                        + " "
                        + contributionWord
                        + ".",
                null
        );
    }
}