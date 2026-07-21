package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GoalDashboardAlertService {

    private static final long INACTIVITY_DAYS = 30;

    private static final BigDecimal
            MILESTONE_PROXIMITY_PERCENTAGE =
            new BigDecimal("5");

    private static final List<BigDecimal> MILESTONES =
            List.of(
                    new BigDecimal("25"),
                    new BigDecimal("50"),
                    new BigDecimal("75"),
                    new BigDecimal("100")
            );

    public Optional<DashboardAlert> create(
            SavingsGoalProgress progress,
            List<SavingsGoalContribution> contributions,
            LocalDate currentDate
    ) {
        Objects.requireNonNull(
                progress,
                "O progresso do objetivo é obrigatório."
        );

        Objects.requireNonNull(
                contributions,
                "As contribuições são obrigatórias."
        );

        Objects.requireNonNull(
                currentDate,
                "A data atual é obrigatória."
        );

        if (progress.status()
                == SavingsGoalProgressStatus.COMPLETED) {
            return Optional.of(
                    new DashboardAlert(
                            "Objetivo alcançado!",
                            "Você chegou ao valor planejado. É hora de celebrar sua conquista.",
                            DashboardAlertLevel.POSITIVE
                    )
            );
        }

        if (contributions.isEmpty()) {
            return Optional.of(
                    new DashboardAlert(
                            "Dê o primeiro passo",
                            "Registre sua primeira contribuição para começar a construir sua conquista.",
                            DashboardAlertLevel.INFORMATION
                    )
            );
        }

        Optional<DashboardAlert> inactivityAlert =
                createInactivityAlert(
                        contributions,
                        currentDate
                );

        if (inactivityAlert.isPresent()) {
            return inactivityAlert;
        }

        return createMilestoneAlert(
                progress.percentage()
        );
    }

    private Optional<DashboardAlert>
    createInactivityAlert(
            List<SavingsGoalContribution> contributions,
            LocalDate currentDate
    ) {
        LocalDate lastContributionDate =
                contributions.stream()
                        .map(contribution ->
                                contribution.contributedAt()
                                        .toLocalDate()
                        )
                        .max(Comparator.naturalOrder())
                        .orElseThrow();

        long inactiveDays = ChronoUnit.DAYS.between(
                lastContributionDate,
                currentDate
        );

        if (inactiveDays < INACTIVITY_DAYS) {
            return Optional.empty();
        }

        return Optional.of(
                new DashboardAlert(
                        "Seu objetivo está esperando por você",
                        "Já se passaram "
                                + inactiveDays
                                + " dias desde a última contribuição.",
                        DashboardAlertLevel.ATTENTION
                )
        );
    }

    private Optional<DashboardAlert>
    createMilestoneAlert(
            BigDecimal currentPercentage
    ) {
        Optional<BigDecimal> nextMilestone =
                MILESTONES.stream()
                        .filter(milestone ->
                                milestone.compareTo(
                                        currentPercentage
                                ) > 0
                        )
                        .findFirst();

        if (nextMilestone.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal milestone =
                nextMilestone.orElseThrow();

        BigDecimal distance =
                milestone.subtract(
                        currentPercentage
                );

        if (distance.compareTo(
                MILESTONE_PROXIMITY_PERCENTAGE
        ) > 0) {
            return Optional.empty();
        }

        String formattedMilestone =
                milestone.stripTrailingZeros()
                        .toPlainString();

        return Optional.of(
                new DashboardAlert(
                        "Você está perto de um marco",
                        "Faltam apenas "
                                + formatPercentage(distance)
                                + " para alcançar "
                                + formattedMilestone
                                + "% do seu objetivo.",
                        DashboardAlertLevel.POSITIVE
                )
        );
    }

    private String formatPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }
}