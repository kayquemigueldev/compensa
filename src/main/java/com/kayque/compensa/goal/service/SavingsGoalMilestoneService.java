package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalMilestone;

import java.math.BigDecimal;
import java.util.Optional;

public class SavingsGoalMilestoneService {

    public Optional<SavingsGoalMilestone>
    findNewMilestone(
            BigDecimal currentPercentage,
            int lastCelebratedMilestone
    ) {
        if (currentPercentage == null) {
            throw new IllegalArgumentException(
                    "A porcentagem atual é obrigatória."
            );
        }

        if (lastCelebratedMilestone < 0
                || lastCelebratedMilestone > 100) {
            throw new IllegalArgumentException(
                    "O último marco celebrado é inválido."
            );
        }

        int normalizedPercentage = currentPercentage
                .min(new BigDecimal("100"))
                .intValue();

        return SavingsGoalMilestone
                .highestReached(normalizedPercentage)
                .filter(milestone ->
                        milestone.percentage()
                                > lastCelebratedMilestone
                );
    }
}