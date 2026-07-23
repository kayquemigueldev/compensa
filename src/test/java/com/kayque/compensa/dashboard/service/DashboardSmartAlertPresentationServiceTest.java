package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardSmartAlertPresentationServiceTest {

    private final DashboardSmartAlertPresentationService service =
            new DashboardSmartAlertPresentationService();

    @Test
    void shouldMapAlertPrioritiesToStyles() {
        List<SmartAlert> alerts = List.of(
                createAlert(
                        "informativo",
                        SmartAlertPriority.INFORMATIONAL
                ),
                createAlert(
                        "atencao",
                        SmartAlertPriority.ATTENTION
                ),
                createAlert(
                        "critico",
                        SmartAlertPriority.CRITICAL
                )
        );

        List<DashboardSmartAlertView> result =
                service.prepare(alerts, 3);

        assertEquals(
                "dashboard-smart-alert-informational",
                result.get(0).styleClass()
        );

        assertEquals(
                "dashboard-smart-alert-attention",
                result.get(1).styleClass()
        );

        assertEquals(
                "dashboard-smart-alert-critical",
                result.get(2).styleClass()
        );
    }

    @Test
    void shouldRespectAlertLimit() {
        List<SmartAlert> alerts = List.of(
                createAlert("primeiro", SmartAlertPriority.CRITICAL),
                createAlert("segundo", SmartAlertPriority.ATTENTION),
                createAlert("terceiro", SmartAlertPriority.INFORMATIONAL)
        );

        List<DashboardSmartAlertView> result =
                service.prepare(alerts, 2);

        assertEquals(2, result.size());
        assertEquals("Título primeiro", result.getFirst().title());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAlerts() {
        assertEquals(
                List.of(),
                service.prepare(List.of(), 3)
        );
    }

    @Test
    void shouldRejectInvalidLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.prepare(List.of(), 0)
        );
    }

    @Test
    void shouldRejectNullList() {
        assertThrows(
                NullPointerException.class,
                () -> service.prepare(null, 3)
        );
    }

    private SmartAlert createAlert(
            String code,
            SmartAlertPriority priority
    ) {
        return new SmartAlert(
                code,
                SmartAlertTopic.BUDGET_USAGE,
                priority,
                "Título " + code,
                "Mensagem " + code
        );
    }
}