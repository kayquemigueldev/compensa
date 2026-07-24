package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseEvaluationAlertRuleTest {

    private final PurchaseEvaluationAlertRule rule =
            new PurchaseEvaluationAlertRule();

    @Test
    void shouldNotCreateAlertWhenEveryPurchaseWasEvaluated() {
        Optional<SmartAlert> alert =
                rule.evaluate(createSnapshot(0));

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldCreateAttentionAlertForOneUnevaluatedPurchase() {
        SmartAlert alert = rule
                .evaluate(createSnapshot(1))
                .orElseThrow();

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                SmartAlertTopic.PURCHASE_EVALUATION,
                alert.topic()
        );

        assertEquals(
                "Uma compra ainda não foi avaliada",
                alert.title()
        );
    }

    @Test
    void shouldCreateAttentionAlertForTwoUnevaluatedPurchases() {
        SmartAlert alert = rule
                .evaluate(createSnapshot(2))
                .orElseThrow();

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "2 compras ainda não foram avaliadas",
                alert.title()
        );
    }

    @Test
    void shouldCreateCriticalAlertForThreeUnevaluatedPurchases() {
        SmartAlert alert = rule
                .evaluate(createSnapshot(3))
                .orElseThrow();

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );

        assertEquals(
                "purchase-evaluation.accumulated",
                alert.code()
        );
    }

    private SmartAlertSnapshot createSnapshot(
            int unevaluatedPurchases
    ) {
        return new SmartAlertSnapshot(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                0,
                unevaluatedPurchases,
                0,
                BigDecimal.ZERO
        );
    }
}