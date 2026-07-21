package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalDashboardAlertServiceTest {

    private final GoalDashboardAlertService service =
            new GoalDashboardAlertService();

    @Test
    void shouldRequestFirstContribution() {
        Optional<DashboardAlert> alert = service.create(
                progress("0", SavingsGoalProgressStatus.NOT_STARTED),
                List.of(),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                DashboardAlertLevel.INFORMATION,
                alert.orElseThrow().level()
        );
    }

    @Test
    void shouldWarnAfterThirtyDaysWithoutContribution() {
        Optional<DashboardAlert> alert = service.create(
                progress("30", SavingsGoalProgressStatus.IN_PROGRESS),
                List.of(
                        contribution("2026-06-01T10:00:00")
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                DashboardAlertLevel.ATTENTION,
                alert.orElseThrow().level()
        );
    }

    @Test
    void shouldNotifyWhenCloseToMilestone() {
        Optional<DashboardAlert> alert = service.create(
                progress("47", SavingsGoalProgressStatus.IN_PROGRESS),
                List.of(
                        contribution("2026-07-20T10:00:00")
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertTrue(
                alert.orElseThrow()
                        .message()
                        .contains("50%")
        );
    }

    @Test
    void shouldNotCreateAlertFarFromMilestone() {
        Optional<DashboardAlert> alert = service.create(
                progress("30", SavingsGoalProgressStatus.IN_PROGRESS),
                List.of(
                        contribution("2026-07-20T10:00:00")
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldCelebrateCompletedGoal() {
        Optional<DashboardAlert> alert = service.create(
                progress("100", SavingsGoalProgressStatus.COMPLETED),
                List.of(
                        contribution("2026-07-20T10:00:00")
                ),
                LocalDate.of(2026, 7, 21)
        );

        assertEquals(
                DashboardAlertLevel.POSITIVE,
                alert.orElseThrow().level()
        );
    }

    private SavingsGoalProgress progress(
            String percentage,
            SavingsGoalProgressStatus status
    ) {
        BigDecimal target = new BigDecimal("1000");

        BigDecimal saved = target
                .multiply(new BigDecimal(percentage))
                .divide(new BigDecimal("100"));

        return new SavingsGoalProgress(
                target,
                saved,
                target.subtract(saved),
                new BigDecimal(percentage),
                status
        );
    }

    private SavingsGoalContribution contribution(
            String dateTime
    ) {
        return new SavingsGoalContribution(
                1,
                new BigDecimal("100"),
                LocalDateTime.parse(dateTime)
        );
    }
}