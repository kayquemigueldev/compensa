package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalConsistency;
import com.kayque.compensa.goal.model.SavingsGoalConsistencyStatus;
import com.kayque.compensa.goal.model.SavingsGoalContribution;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SavingsGoalConsistencyService {

    public SavingsGoalConsistency calculate(
            List<SavingsGoalContribution> contributions,
            LocalDate referenceDate
    ) {
        Objects.requireNonNull(
                contributions,
                "As contribuições são obrigatórias."
        );

        Objects.requireNonNull(
                referenceDate,
                "A data de referência é obrigatória."
        );

        YearMonth referenceMonth =
                YearMonth.from(referenceDate);

        BigDecimal contributedThisMonth =
                calculateContributedThisMonth(
                        contributions,
                        referenceMonth
                );

        int contributionsThisMonth =
                countContributionsThisMonth(
                        contributions,
                        referenceMonth
                );

        int consecutiveMonths =
                calculateConsecutiveMonths(
                        contributions,
                        referenceMonth
                );

        SavingsGoalConsistencyStatus status =
                determineStatus(
                        contributions,
                        contributionsThisMonth,
                        consecutiveMonths
                );

        return new SavingsGoalConsistency(
                contributedThisMonth,
                contributionsThisMonth,
                consecutiveMonths,
                status
        );
    }

    private BigDecimal calculateContributedThisMonth(
            List<SavingsGoalContribution> contributions,
            YearMonth referenceMonth
    ) {
        return contributions.stream()
                .filter(contribution ->
                        belongsToMonth(
                                contribution,
                                referenceMonth
                        )
                )
                .map(SavingsGoalContribution::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private int countContributionsThisMonth(
            List<SavingsGoalContribution> contributions,
            YearMonth referenceMonth
    ) {
        return Math.toIntExact(
                contributions.stream()
                        .filter(contribution ->
                                belongsToMonth(
                                        contribution,
                                        referenceMonth
                                )
                        )
                        .count()
        );
    }

    private int calculateConsecutiveMonths(
            List<SavingsGoalContribution> contributions,
            YearMonth referenceMonth
    ) {
        Set<YearMonth> activeMonths = new HashSet<>();

        contributions.forEach(contribution ->
                activeMonths.add(
                        YearMonth.from(
                                contribution.contributedAt()
                        )
                )
        );

        int consecutiveMonths = 0;
        YearMonth month = referenceMonth;

        while (activeMonths.contains(month)) {
            consecutiveMonths++;
            month = month.minusMonths(1);
        }

        return consecutiveMonths;
    }

    private SavingsGoalConsistencyStatus determineStatus(
            List<SavingsGoalContribution> contributions,
            int contributionsThisMonth,
            int consecutiveMonths
    ) {
        if (contributions.isEmpty()) {
            return SavingsGoalConsistencyStatus
                    .NO_CONTRIBUTIONS;
        }

        if (contributionsThisMonth == 0) {
            return SavingsGoalConsistencyStatus.PAUSED;
        }

        if (consecutiveMonths >= 2) {
            return SavingsGoalConsistencyStatus.CONSISTENT;
        }

        return SavingsGoalConsistencyStatus
                .STARTED_THIS_MONTH;
    }

    private boolean belongsToMonth(
            SavingsGoalContribution contribution,
            YearMonth month
    ) {
        return YearMonth.from(
                contribution.contributedAt()
        ).equals(month);
    }
}