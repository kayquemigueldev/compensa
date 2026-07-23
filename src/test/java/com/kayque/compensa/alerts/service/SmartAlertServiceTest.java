package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmartAlertServiceTest {

    @Test
    void shouldGenerateAlertsFromCurrentSnapshot() {
        SmartAlertSnapshot snapshot =
                createSnapshot();

        SmartAlert expectedAlert = new SmartAlert(
                "budget.test",
                SmartAlertTopic.BUDGET_USAGE,
                SmartAlertPriority.INFORMATIONAL,
                "Orçamento saudável",
                "Seu orçamento está sob controle."
        );

        SmartAlertSnapshotSource source =
                () -> snapshot;

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(currentSnapshot ->
                                Optional.of(expectedAlert)
                        )
                );

        SmartAlertService service =
                new SmartAlertService(
                        source,
                        engine
                );

        assertEquals(
                List.of(expectedAlert),
                service.generateAlerts()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleGeneratesAlert() {
        SmartAlertSnapshotSource source =
                this::createSnapshot;

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(snapshot -> Optional.empty())
                );

        SmartAlertService service =
                new SmartAlertService(
                        source,
                        engine
                );

        assertEquals(
                List.of(),
                service.generateAlerts()
        );
    }

    @Test
    void shouldRejectNullSnapshotSource() {
        SmartAlertEngine engine =
                new SmartAlertEngine(List.of());

        assertThrows(
                NullPointerException.class,
                () -> new SmartAlertService(
                        null,
                        engine
                )
        );
    }

    @Test
    void shouldRejectNullEngine() {
        SmartAlertSnapshotSource source =
                this::createSnapshot;

        assertThrows(
                NullPointerException.class,
                () -> new SmartAlertService(
                        source,
                        null
                )
        );
    }

    private SmartAlertSnapshot createSnapshot() {
        return new SmartAlertSnapshot(
                new BigDecimal("40"),
                new BigDecimal("300"),
                new BigDecimal("150"),
                new BigDecimal("500"),
                new BigDecimal("25"),
                new BigDecimal("20"),
                2,
                1,
                0,
                0,
                240,
                new BigDecimal("100")
        );
    }
}