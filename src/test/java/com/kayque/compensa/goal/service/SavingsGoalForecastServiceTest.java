package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsGoalForecastServiceTest {

    private final SavingsGoalForecastService service =
            new SavingsGoalForecastService();

    @Test
    void shouldRequestHistoryWhenThereAreNoContributions() {
        SavingsGoalForecast forecast = service.calculate(
                goal("1000", "200"),
                List.of(),
                LocalDate.of(2026, 2, 1)
        );

        assertEquals(
                SavingsGoalForecastStatus.NO_HISTORY,
                forecast.status()
        );

        assertTrue(forecast.completionDate().isEmpty());
    }

    @Test
    void shouldEstimateNumberOfSimilarContributions() {
        SavingsGoalForecast forecast = service.calculate(
                goal("1000", "200"),
                List.of(
                        contribution(
                                1,
                                "200",
                                "2026-01-15T10:00:00"
                        )
                ),
                LocalDate.of(2026, 2, 1)
        );

        assertEquals(
                SavingsGoalForecastStatus.CONTRIBUTIONS_NEEDED,
                forecast.status()
        );

        assertTrue(
                forecast.message().contains("4 contribuições")
        );
    }

    @Test
    void shouldEstimateCompletionDateFromHistory() {
        SavingsGoalForecast forecast = service.calculate(
                goal("1000", "400"),
                List.of(
                        contribution(
                                1,
                                "100",
                                "2026-01-01T10:00:00"
                        ),
                        contribution(
                                2,
                                "200",
                                "2026-01-31T10:00:00"
                        )
                ),
                LocalDate.of(2026, 2, 1)
        );

        assertEquals(
                SavingsGoalForecastStatus.ESTIMATED_DATE,
                forecast.status()
        );

        assertEquals(
                LocalDate.of(2026, 4, 2),
                forecast.completionDate().orElseThrow()
        );
    }

    @Test
    void shouldRecognizeCompletedGoal() {
        SavingsGoalForecast forecast = service.calculate(
                goal("1000", "1000"),
                List.of(),
                LocalDate.of(2026, 2, 1)
        );

        assertEquals(
                SavingsGoalForecastStatus.COMPLETED,
                forecast.status()
        );
    }

    private SavingsGoal goal(
            String target,
            String saved
    ) {
        return new SavingsGoal(
                "Montar meu computador",
                new BigDecimal(target),
                new BigDecimal(saved)
        );
    }

    private SavingsGoalContribution contribution(
            long id,
            String amount,
            String dateTime
    ) {
        return new SavingsGoalContribution(
                id,
                new BigDecimal(amount),
                LocalDateTime.parse(dateTime)
        );
    }
}