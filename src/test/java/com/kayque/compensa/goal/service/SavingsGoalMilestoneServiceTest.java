package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalMilestone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalMilestoneServiceTest {

    private final SavingsGoalMilestoneService service =
            new SavingsGoalMilestoneService();

    @Test
    void shouldCelebrateTwentyFivePercent() {
        Optional<SavingsGoalMilestone> milestone =
                service.findNewMilestone(
                        new BigDecimal("25"),
                        0
                );

        assertEquals(
                SavingsGoalMilestone.TWENTY_FIVE,
                milestone.orElseThrow()
        );
    }

    @Test
    void shouldCelebrateHighestReachedMilestone() {
        Optional<SavingsGoalMilestone> milestone =
                service.findNewMilestone(
                        new BigDecimal("76"),
                        25
                );

        assertEquals(
                SavingsGoalMilestone.SEVENTY_FIVE,
                milestone.orElseThrow()
        );
    }

    @Test
    void shouldNotRepeatCelebratedMilestone() {
        Optional<SavingsGoalMilestone> milestone =
                service.findNewMilestone(
                        new BigDecimal("29"),
                        25
                );

        assertTrue(milestone.isEmpty());
    }

    @Test
    void shouldCelebrateCompletedGoal() {
        Optional<SavingsGoalMilestone> milestone =
                service.findNewMilestone(
                        new BigDecimal("100"),
                        75
                );

        assertEquals(
                SavingsGoalMilestone.COMPLETED,
                milestone.orElseThrow()
        );
    }
}