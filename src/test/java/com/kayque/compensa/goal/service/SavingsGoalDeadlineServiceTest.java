package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalDeadlineStatus;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavingsGoalDeadlineServiceTest {

    private final SavingsGoalDeadlineService service =
            new SavingsGoalDeadlineService();

    private final LocalDate targetDate =
            LocalDate.of(2026, 12, 20);

    @Test
    void shouldReportMissingTargetDate() {
        SavingsGoal goal = new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1250")
        );

        SavingsGoalForecast forecast = forecastWithDate(
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(
                SavingsGoalDeadlineStatus.NO_TARGET_DATE,
                service.evaluate(goal, forecast)
        );
    }

    @Test
    void shouldReportUnavailableForecast() {
        SavingsGoal goal = createGoal();

        SavingsGoalForecast forecast =
                new SavingsGoalForecast(
                        SavingsGoalForecastStatus.NO_HISTORY,
                        "Registre contribuições para gerar uma previsão.",
                        null
                );

        assertEquals(
                SavingsGoalDeadlineStatus.FORECAST_UNAVAILABLE,
                service.evaluate(goal, forecast)
        );
    }

    @Test
    void shouldReportAheadOfSchedule() {
        SavingsGoalForecast forecast = forecastWithDate(
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(
                SavingsGoalDeadlineStatus.AHEAD_OF_SCHEDULE,
                service.evaluate(createGoal(), forecast)
        );
    }

    @Test
    void shouldReportOnSchedule() {
        SavingsGoalForecast forecast =
                forecastWithDate(targetDate);

        assertEquals(
                SavingsGoalDeadlineStatus.ON_SCHEDULE,
                service.evaluate(createGoal(), forecast)
        );
    }

    @Test
    void shouldReportBehindSchedule() {
        SavingsGoalForecast forecast = forecastWithDate(
                LocalDate.of(2027, 1, 20)
        );

        assertEquals(
                SavingsGoalDeadlineStatus.BEHIND_SCHEDULE,
                service.evaluate(createGoal(), forecast)
        );
    }

    @Test
    void shouldReportCompletedGoal() {
        SavingsGoalForecast forecast =
                new SavingsGoalForecast(
                        SavingsGoalForecastStatus.COMPLETED,
                        "Conquista alcançada.",
                        null
                );

        assertEquals(
                SavingsGoalDeadlineStatus.COMPLETED,
                service.evaluate(createGoal(), forecast)
        );
    }

    private SavingsGoal createGoal() {
        return new SavingsGoal(
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1250"),
                targetDate
        );
    }

    private SavingsGoalForecast forecastWithDate(
            LocalDate completionDate
    ) {
        return new SavingsGoalForecast(
                SavingsGoalForecastStatus.ESTIMATED_DATE,
                "Previsão calculada.",
                completionDate
        );
    }
}