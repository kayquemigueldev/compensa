package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.dashboard.model.PurchaseAttentionSummary;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseDashboardAlertServiceTest {

    private final PurchaseDashboardAlertService service =
            new PurchaseDashboardAlertService();

    @Test
    void shouldNotCreateAlertWithoutPendingItems() {
        Optional<DashboardAlert> alert =
                service.create(
                        new PurchaseAttentionSummary(0, 0)
                );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldAlertAboutWaitingDecision() {
        DashboardAlert alert = service.create(
                new PurchaseAttentionSummary(1, 0)
        ).orElseThrow();

        assertEquals(
                DashboardAlertLevel.INFORMATION,
                alert.level()
        );

        assertTrue(
                alert.message().contains("uma compra")
        );
    }

    @Test
    void shouldAlertAboutUnevaluatedPurchases() {
        DashboardAlert alert = service.create(
                new PurchaseAttentionSummary(0, 3)
        ).orElseThrow();

        assertEquals(
                DashboardAlertLevel.ATTENTION,
                alert.level()
        );

        assertTrue(
                alert.message().contains("3 compras")
        );
    }

    @Test
    void shouldCombineBothPendingItems() {
        DashboardAlert alert = service.create(
                new PurchaseAttentionSummary(2, 3)
        ).orElseThrow();

        assertTrue(
                alert.message().contains("2 compras")
        );

        assertTrue(
                alert.message().contains("3 compras")
        );
    }
}