package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardSmartAlertPresentationServiceAllAlertsTest {

    private final DashboardSmartAlertPresentationService service =
            new DashboardSmartAlertPresentationService();

    @Test
    void shouldPrepareEveryAlertWithoutApplyingDashboardLimit() {
        List<SmartAlert> alerts = List.of(
                createAlert(
                        "budget-usage",
                        SmartAlertTopic.BUDGET_USAGE
                ),
                createAlert(
                        "goal-progress",
                        SmartAlertTopic.FINANCIAL_GOAL_PROGRESS
                ),
                createAlert(
                        "purchase-evaluation",
                        SmartAlertTopic.PURCHASE_EVALUATION
                ),
                createAlert(
                        "work-time",
                        SmartAlertTopic.WORK_TIME
                )
        );

        List<DashboardSmartAlertView> result =
                service.prepareAll(alerts);

        assertEquals(4, result.size());

        assertEquals(
                List.of(
                        "budget-usage",
                        "goal-progress",
                        "purchase-evaluation",
                        "work-time"
                ),
                result.stream()
                        .map(DashboardSmartAlertView::code)
                        .toList()
        );
    }

    @Test
    void shouldRejectNullAlertList() {
        assertThrows(
                NullPointerException.class,
                () -> service.prepareAll(null)
        );
    }

    @Test
    void shouldRejectListContainingNullAlert() {
        List<SmartAlert> alerts =
                new java.util.ArrayList<>();

        alerts.add(
                createAlert(
                        "budget-usage",
                        SmartAlertTopic.BUDGET_USAGE
                )
        );

        alerts.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.prepareAll(alerts)
        );
    }

    private SmartAlert createAlert(
            String code,
            SmartAlertTopic topic
    ) {
        return new SmartAlert(
                code,
                topic,
                SmartAlertPriority.ATTENTION,
                "Alerta de teste",
                "Mensagem de teste"
        );
    }
}