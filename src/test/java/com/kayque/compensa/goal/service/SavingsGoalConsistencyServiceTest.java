package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoalConsistency;
import com.kayque.compensa.goal.model.SavingsGoalConsistencyStatus;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavingsGoalConsistencyServiceTest {

    private final SavingsGoalConsistencyService service =
            new SavingsGoalConsistencyService();

    @Test
    void shouldReturnNoContributionsWhenHistoryIsEmpty() {
        SavingsGoalConsistency result = service.calculate(
                List.of(),
                LocalDate.of(2026, 7, 25)
        );

        assertEquals(
                SavingsGoalConsistencyStatus.NO_CONTRIBUTIONS,
                result.status()
        );

        assertEquals(
                0,
                result.contributedThisMonth()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(0, result.contributionsThisMonth());
        assertEquals(0, result.consecutiveMonths());
    }

    @Test
    void shouldCalculateContributionsFromCurrentMonth() {
        List<SavingsGoalContribution> contributions =
                List.of(
                        contribution(
                                1,
                                "100.00",
                                LocalDateTime.of(
                                        2026, 7, 5, 10, 0
                                )
                        ),

                        contribution(
                                2,
                                "250.00",
                                LocalDateTime.of(
                                        2026, 7, 20, 10, 0
                                )
                        ),

                        contribution(
                                3,
                                "500.00",
                                LocalDateTime.of(
                                        2026, 6, 10, 10, 0
                                )
                        )
                );

        SavingsGoalConsistency result = service.calculate(
                contributions,
                LocalDate.of(2026, 7, 25)
        );

        assertEquals(
                0,
                result.contributedThisMonth()
                        .compareTo(new BigDecimal("350.00"))
        );

        assertEquals(2, result.contributionsThisMonth());
    }

    @Test
    void shouldRecognizeConsistentConsecutiveMonths() {
        List<SavingsGoalContribution> contributions =
                List.of(
                        contribution(
                                1,
                                "100.00",
                                LocalDateTime.of(
                                        2026, 7, 5, 10, 0
                                )
                        ),

                        contribution(
                                2,
                                "200.00",
                                LocalDateTime.of(
                                        2026, 6, 5, 10, 0
                                )
                        ),

                        contribution(
                                3,
                                "300.00",
                                LocalDateTime.of(
                                        2026, 5, 5, 10, 0
                                )
                        )
                );

        SavingsGoalConsistency result = service.calculate(
                contributions,
                LocalDate.of(2026, 7, 25)
        );

        assertEquals(
                SavingsGoalConsistencyStatus.CONSISTENT,
                result.status()
        );

        assertEquals(3, result.consecutiveMonths());
    }

    @Test
    void shouldReturnPausedWhenCurrentMonthHasNoContribution() {
        List<SavingsGoalContribution> contributions =
                List.of(
                        contribution(
                                1,
                                "200.00",
                                LocalDateTime.of(
                                        2026, 6, 5, 10, 0
                                )
                        )
                );

        SavingsGoalConsistency result = service.calculate(
                contributions,
                LocalDate.of(2026, 7, 25)
        );

        assertEquals(
                SavingsGoalConsistencyStatus.PAUSED,
                result.status()
        );

        assertEquals(0, result.consecutiveMonths());
    }

    private SavingsGoalContribution contribution(
            long id,
            String amount,
            LocalDateTime date
    ) {
        return new SavingsGoalContribution(
                id,
                new BigDecimal(amount),
                date
        );
    }
}