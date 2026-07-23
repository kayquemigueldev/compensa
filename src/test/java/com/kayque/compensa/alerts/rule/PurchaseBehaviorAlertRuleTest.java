package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseBehaviorAlertRuleTest {

    private final PurchaseBehaviorAlertRule rule =
            new PurchaseBehaviorAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutPurchaseDecisions() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot(0, 0)
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateAttentionAlertForManyPurchases() {
        SmartAlert alert = rule.evaluate(
                snapshot(6, 0)
        ).orElseThrow();

        assertEquals(
                "purchase-behavior.high-purchases",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "Você realizou 6 compras e evitou 0 compras. "
                        + "Vale revisar as próximas decisões com mais calma.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAttentionWhenPurchasesStronglyPredominate() {
        SmartAlert alert = rule.evaluate(
                snapshot(7, 2)
        ).orElseThrow();

        assertEquals(
                "purchase-behavior.high-purchases",
                alert.code()
        );
    }

    @Test
    void shouldGeneratePositiveAlertWhenAvoidedPurchasesPredominate() {
        SmartAlert alert = rule.evaluate(
                snapshot(2, 5)
        ).orElseThrow();

        assertEquals(
                "purchase-behavior.positive-balance",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "Você evitou 5 compras e realizou 2 compras.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateInformationalAlertForAvoidedPurchase() {
        SmartAlert alert = rule.evaluate(
                snapshot(3, 1)
        ).orElseThrow();

        assertEquals(
                "purchase-behavior.avoided-purchases",
                alert.code()
        );

        assertEquals(
                "Você evitou 1 compra neste período.",
                alert.message()
        );
    }

    @Test
    void shouldRejectMissingSnapshot() {
        assertThrows(
                NullPointerException.class,
                () -> rule.evaluate(null)
        );
    }

    private SmartAlertSnapshot snapshot(
            int purchasesMade,
            int purchasesAvoided
    ) {
        return new SmartAlertSnapshot(
                BigDecimal.ZERO,
                new BigDecimal("500"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                purchasesMade,
                purchasesAvoided,
                0,
                0,
                0,
                BigDecimal.ZERO
        );
    }
}