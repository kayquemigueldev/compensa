package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardHighlight;
import com.kayque.compensa.dashboard.model.DashboardHighlightType;
import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardHighlightServiceTest {

    private final DashboardHighlightService service =
            new DashboardHighlightService();

    @Test
    void shouldPrioritizeCompletedGoal() {
        DashboardHighlight highlight = service.create(
                summary(2, BigDecimal.ZERO),
                "Montar meu computador",
                progress(
                        "5000",
                        "5000",
                        "0",
                        "100",
                        SavingsGoalProgressStatus.COMPLETED
                )
        );

        assertEquals(
                DashboardHighlightType.SUCCESS,
                highlight.type()
        );

        assertEquals(
                "Objetivo alcançado!",
                highlight.title()
        );
    }

    @Test
    void shouldShowGoalMilestone() {
        DashboardHighlight highlight = service.create(
                summary(1, new BigDecimal("100")),
                "Montar meu computador",
                progress(
                        "5000",
                        "1250",
                        "3750",
                        "25",
                        SavingsGoalProgressStatus.IN_PROGRESS
                )
        );

        assertEquals(
                DashboardHighlightType.GOAL,
                highlight.type()
        );

        assertEquals(
                "25% da conquista alcançados",
                highlight.title()
        );
    }

    @Test
    void shouldShowWaitingDecisionsWithoutMilestone() {
        DashboardHighlight highlight = service.create(
                summary(3, BigDecimal.ZERO),
                null,
                null
        );

        assertEquals(
                DashboardHighlightType.WARNING,
                highlight.type()
        );
    }

    @Test
    void shouldShowPreservedValue() {
        DashboardHighlight highlight = service.create(
                summary(0, new BigDecimal("350")),
                null,
                null
        );

        assertEquals(
                DashboardHighlightType.PRESERVED_VALUE,
                highlight.type()
        );
    }

    @Test
    void shouldShowDefaultMessageWhenThereIsNoActivity() {
        DashboardHighlight highlight = service.create(
                summary(0, BigDecimal.ZERO),
                null,
                null
        );

        assertEquals(
                DashboardHighlightType.DEFAULT,
                highlight.type()
        );
    }

    private DashboardSummary summary(
            long waitingDecisions,
            BigDecimal preservedValue
    ) {
        return new DashboardSummary(
                waitingDecisions,
                0,
                preservedValue.signum() > 0 ? 1 : 0,
                waitingDecisions,
                preservedValue,
                0
        );
    }

    private SavingsGoalProgress progress(
            String target,
            String saved,
            String remaining,
            String percentage,
            SavingsGoalProgressStatus status
    ) {
        return new SavingsGoalProgress(
                new BigDecimal(target),
                new BigDecimal(saved),
                new BigDecimal(remaining),
                new BigDecimal(percentage),
                status
        );
    }
}