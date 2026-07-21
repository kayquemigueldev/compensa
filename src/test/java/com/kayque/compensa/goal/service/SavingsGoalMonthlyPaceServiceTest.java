package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPace;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPaceStatus;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalMonthlyPaceServiceTest {

    private final SavingsGoalMonthlyPaceService service =
            new SavingsGoalMonthlyPaceService();

    @Test
    void shouldCalculateCurrentMonthProgress() {
        SavingsGoalMonthlyPace pace = service.calculate(
                createActivePlan(),
                List.of(
                        createContribution(
                                1,
                                "50",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        19,
                                        10,
                                        30
                                )
                        ),
                        createContribution(
                                2,
                                "100",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        21,
                                        14,
                                        0
                                )
                        )
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalMonthlyPaceStatus.IN_PROGRESS,
                pace.status()
        );

        assertEquals(
                new BigDecimal("625.00"),
                pace.requiredMonthlyAmount()
        );

        assertEquals(
                new BigDecimal("150"),
                pace.contributedThisMonth()
        );

        assertEquals(
                new BigDecimal("475.00"),
                pace.remainingThisMonth()
        );

        assertTrue(pace.hasActivePlan());
        assertFalse(pace.hasReachedMonthlyTarget());
    }

    @Test
    void shouldIgnoreContributionsFromOtherMonths() {
        SavingsGoalMonthlyPace pace = service.calculate(
                createActivePlan(),
                List.of(
                        createContribution(
                                1,
                                "200",
                                LocalDateTime.of(
                                        2026,
                                        6,
                                        30,
                                        20,
                                        0
                                )
                        ),
                        createContribution(
                                2,
                                "50",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        1,
                                        8,
                                        0
                                )
                        ),
                        createContribution(
                                3,
                                "300",
                                LocalDateTime.of(
                                        2025,
                                        7,
                                        15,
                                        12,
                                        0
                                )
                        )
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                new BigDecimal("50"),
                pace.contributedThisMonth()
        );

        assertEquals(
                new BigDecimal("575.00"),
                pace.remainingThisMonth()
        );
    }

    @Test
    void shouldIndicateWhenMonthlyTargetHasNotStarted() {
        SavingsGoalMonthlyPace pace = service.calculate(
                createActivePlan(),
                List.of(),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalMonthlyPaceStatus.NOT_STARTED,
                pace.status()
        );

        assertEquals(
                BigDecimal.ZERO,
                pace.contributedThisMonth()
        );

        assertEquals(
                new BigDecimal("625.00"),
                pace.remainingThisMonth()
        );
    }

    @Test
    void shouldIndicateWhenMonthlyTargetWasReached() {
        SavingsGoalMonthlyPace pace = service.calculate(
                createActivePlan(),
                List.of(
                        createContribution(
                                1,
                                "700",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        20,
                                        18,
                                        0
                                )
                        )
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalMonthlyPaceStatus
                        .MONTHLY_TARGET_REACHED,
                pace.status()
        );

        assertEquals(
                BigDecimal.ZERO,
                pace.remainingThisMonth()
        );

        assertTrue(pace.hasReachedMonthlyTarget());
    }

    @Test
    void shouldReturnUnavailableWithoutActivePlan() {
        SavingsGoalTargetPlan targetPlan =
                new SavingsGoalTargetPlan(
                        SavingsGoalTargetPlanStatus.NO_TARGET_DATE,
                        new BigDecimal("3750"),
                        0,
                        BigDecimal.ZERO
                );

        SavingsGoalMonthlyPace pace = service.calculate(
                targetPlan,
                List.of(),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalMonthlyPaceStatus.NO_ACTIVE_PLAN,
                pace.status()
        );

        assertFalse(pace.hasActivePlan());
    }

    @Test
    void shouldIndicateWhenGoalIsCompleted() {
        SavingsGoalTargetPlan targetPlan =
                new SavingsGoalTargetPlan(
                        SavingsGoalTargetPlanStatus.COMPLETED,
                        BigDecimal.ZERO,
                        0,
                        BigDecimal.ZERO
                );

        SavingsGoalMonthlyPace pace = service.calculate(
                targetPlan,
                List.of(),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                SavingsGoalMonthlyPaceStatus.GOAL_COMPLETED,
                pace.status()
        );

        assertEquals(
                BigDecimal.ZERO,
                pace.remainingThisMonth()
        );
    }

    private SavingsGoalTargetPlan createActivePlan() {
        return new SavingsGoalTargetPlan(
                SavingsGoalTargetPlanStatus.ACTIVE,
                new BigDecimal("3750"),
                6,
                new BigDecimal("625.00")
        );
    }

    private SavingsGoalContribution createContribution(
            long id,
            String amount,
            LocalDateTime contributedAt
    ) {
        return new SavingsGoalContribution(
                id,
                new BigDecimal(amount),
                contributedAt
        );
    }
}