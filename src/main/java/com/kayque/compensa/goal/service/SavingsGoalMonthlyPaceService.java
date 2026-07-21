package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPace;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPaceStatus;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public class SavingsGoalMonthlyPaceService {

    public SavingsGoalMonthlyPace calculate(
            SavingsGoalTargetPlan targetPlan,
            List<SavingsGoalContribution> contributions,
            LocalDate currentDate
    ) {
        Objects.requireNonNull(
                targetPlan,
                "O planejamento do objetivo é obrigatório."
        );

        Objects.requireNonNull(
                contributions,
                "As contribuições são obrigatórias."
        );

        Objects.requireNonNull(
                currentDate,
                "A data atual é obrigatória."
        );

        if (targetPlan.status()
                == SavingsGoalTargetPlanStatus.COMPLETED) {
            return createResult(
                    SavingsGoalMonthlyPaceStatus.GOAL_COMPLETED,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        if (!targetPlan.hasMonthlyPlan()) {
            return createResult(
                    SavingsGoalMonthlyPaceStatus.NO_ACTIVE_PLAN,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal contributedThisMonth =
                calculateCurrentMonthContributions(
                        contributions,
                        currentDate
                );

        BigDecimal requiredMonthlyAmount =
                targetPlan.requiredMonthlyAmount();

        BigDecimal remainingThisMonth =
                requiredMonthlyAmount
                        .subtract(contributedThisMonth)
                        .max(BigDecimal.ZERO);

        SavingsGoalMonthlyPaceStatus status =
                determineStatus(
                        contributedThisMonth,
                        requiredMonthlyAmount
                );

        return createResult(
                status,
                requiredMonthlyAmount,
                contributedThisMonth,
                remainingThisMonth
        );
    }

    private BigDecimal calculateCurrentMonthContributions(
            List<SavingsGoalContribution> contributions,
            LocalDate currentDate
    ) {
        YearMonth currentMonth =
                YearMonth.from(currentDate);

        return contributions.stream()
                .peek(contribution ->
                        Objects.requireNonNull(
                                contribution,
                                "A contribuição não pode ser nula."
                        )
                )
                .filter(contribution ->
                        YearMonth.from(
                                contribution.contributedAt()
                        ).equals(currentMonth)
                )
                .map(SavingsGoalContribution::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private SavingsGoalMonthlyPaceStatus determineStatus(
            BigDecimal contributedThisMonth,
            BigDecimal requiredMonthlyAmount
    ) {
        if (contributedThisMonth.compareTo(
                requiredMonthlyAmount
        ) >= 0) {
            return SavingsGoalMonthlyPaceStatus
                    .MONTHLY_TARGET_REACHED;
        }

        if (contributedThisMonth.compareTo(
                BigDecimal.ZERO
        ) > 0) {
            return SavingsGoalMonthlyPaceStatus.IN_PROGRESS;
        }

        return SavingsGoalMonthlyPaceStatus.NOT_STARTED;
    }

    private SavingsGoalMonthlyPace createResult(
            SavingsGoalMonthlyPaceStatus status,
            BigDecimal requiredMonthlyAmount,
            BigDecimal contributedThisMonth,
            BigDecimal remainingThisMonth
    ) {
        return new SavingsGoalMonthlyPace(
                status,
                requiredMonthlyAmount,
                contributedThisMonth,
                remainingThisMonth
        );
    }
}