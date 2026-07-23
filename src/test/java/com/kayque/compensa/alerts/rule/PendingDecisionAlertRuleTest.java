package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingDecisionAlertRuleTest {

    private final PendingDecisionAlertRule rule =
            new PendingDecisionAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutPendingDecisions() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot(0, 0)
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateAttentionAlertForOnePendingDecision() {
        SmartAlert alert = rule.evaluate(
                snapshot(1, 0)
        ).orElseThrow();

        assertEquals(
                "pending-decisions.waiting",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.PENDING_DECISIONS,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "Você possui uma decisão aguardando resposta",
                alert.title()
        );

        assertEquals(
                "Quando estiver pronto, volte à decisão "
                        + "e registre o que escolheu.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAttentionAlertForMultiplePendingDecisions() {
        SmartAlert alert = rule.evaluate(
                snapshot(3, 0)
        ).orElseThrow();

        assertEquals(
                "Você possui 3 decisões aguardando resposta",
                alert.title()
        );

        assertEquals(
                "Quando estiver pronto, volte às decisões "
                        + "e registre o que escolheu.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateCriticalAlertForOneOverdueDecision() {
        SmartAlert alert = rule.evaluate(
                snapshot(1, 1)
        ).orElseThrow();

        assertEquals(
                "pending-decisions.overdue",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );

        assertEquals(
                "Uma decisão está aguardando há mais de 7 dias",
                alert.title()
        );
    }

    @Test
    void shouldGenerateCriticalAlertForMultipleOverdueDecisions() {
        SmartAlert alert = rule.evaluate(
                snapshot(4, 2)
        ).orElseThrow();

        assertEquals(
                "2 decisões estão aguardando há mais de 7 dias",
                alert.title()
        );

        assertEquals(
                "Reavalie essas decisões antes que elas deixem "
                        + "de representar sua situação atual.",
                alert.message()
        );
    }

    @Test
    void shouldPrioritizeOverdueAlertOverRegularPendingAlert() {
        SmartAlert alert = rule.evaluate(
                snapshot(5, 1)
        ).orElseThrow();

        assertEquals(
                "pending-decisions.overdue",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
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
            int pendingDecisions,
            int overduePendingDecisions
    ) {
        return new SmartAlertSnapshot(
                BigDecimal.ZERO,
                new BigDecimal("500"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                pendingDecisions,
                overduePendingDecisions,
                0,
                BigDecimal.ZERO
        );
    }
}