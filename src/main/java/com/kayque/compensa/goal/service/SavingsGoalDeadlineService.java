package com.kayque.compensa.goal.service;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalDeadlineStatus;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;

import java.time.LocalDate;
import java.util.Objects;

public class SavingsGoalDeadlineService {

    public SavingsGoalDeadlineStatus evaluate(
            SavingsGoal goal,
            SavingsGoalForecast forecast
    ) {
        Objects.requireNonNull(
                goal,
                "O objetivo é obrigatório."
        );

        Objects.requireNonNull(
                forecast,
                "A previsão é obrigatória."
        );

        if (forecast.status()
                == SavingsGoalForecastStatus.COMPLETED) {
            return SavingsGoalDeadlineStatus.COMPLETED;
        }

        if (!goal.hasTargetDate()) {
            return SavingsGoalDeadlineStatus.NO_TARGET_DATE;
        }

        if (forecast.completionDate().isEmpty()) {
            return SavingsGoalDeadlineStatus
                    .FORECAST_UNAVAILABLE;
        }

        LocalDate targetDate = goal.targetDate();

        LocalDate forecastDate =
                forecast.completionDate()
                        .orElseThrow();

        if (forecastDate.isBefore(targetDate)) {
            return SavingsGoalDeadlineStatus
                    .AHEAD_OF_SCHEDULE;
        }

        if (forecastDate.isEqual(targetDate)) {
            return SavingsGoalDeadlineStatus
                    .ON_SCHEDULE;
        }

        return SavingsGoalDeadlineStatus
                .BEHIND_SCHEDULE;
    }
}