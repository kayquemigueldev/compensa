package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.navigation.NavigationTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardAlertNavigationServiceTest {

    private final DashboardAlertNavigationService service =
            new DashboardAlertNavigationService();

    @Test
    void shouldNavigateFinancialAlertsToFinancialProfile() {
        assertEquals(
                NavigationTarget.FINANCIAL_PROFILE,
                service.resolve(
                        SmartAlertTopic.BUDGET_USAGE
                )
        );

        assertEquals(
                NavigationTarget.FINANCIAL_PROFILE,
                service.resolve(
                        SmartAlertTopic.MONTHLY_SAVINGS_GOAL
                )
        );
    }

    @Test
    void shouldNavigateGoalProgressToSavingsGoal() {
        assertEquals(
                NavigationTarget.SAVINGS_GOAL,
                service.resolve(
                        SmartAlertTopic.FINANCIAL_GOAL_PROGRESS
                )
        );
    }

    @Test
    void shouldNavigateDecisionAlertsToHistory() {
        assertEquals(
                NavigationTarget.HISTORY,
                service.resolve(
                        SmartAlertTopic.PENDING_DECISIONS
                )
        );

        assertEquals(
                NavigationTarget.HISTORY,
                service.resolve(
                        SmartAlertTopic.PURCHASE_EVALUATION
                )
        );

        assertEquals(
                NavigationTarget.HISTORY,
                service.resolve(
                        SmartAlertTopic.WORK_TIME
                )
        );

        assertEquals(
                NavigationTarget.HISTORY,
                service.resolve(
                        SmartAlertTopic.PRESERVED_VALUE
                )
        );
    }

    @Test
    void shouldNavigatePurchaseBehaviorToInsights() {
        assertEquals(
                NavigationTarget.INSIGHTS,
                service.resolve(
                        SmartAlertTopic.PURCHASE_BEHAVIOR
                )
        );
    }

    @Test
    void shouldRejectNullTopic() {
        assertThrows(
                NullPointerException.class,
                () -> service.resolve(null)
        );
    }
}