package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.alerts.rule.SmartAlertRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertEngineTest {

    @Test
    void shouldGenerateAlertsFromRules() {
        SmartAlertRule budgetRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.attention",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.ATTENTION
                        )
                );

        SmartAlertRule goalRule = snapshot ->
                Optional.of(
                        createAlert(
                                "goal.information",
                                SmartAlertTopic
                                        .FINANCIAL_GOAL_PROGRESS,
                                SmartAlertPriority.INFORMATIONAL
                        )
                );

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(
                                budgetRule,
                                goalRule
                        )
                );

        List<SmartAlert> alerts =
                engine.generate(createSnapshot());

        assertEquals(2, alerts.size());
    }

    @Test
    void shouldIgnoreRulesWithoutAlert() {
        SmartAlertRule emptyRule =
                snapshot -> Optional.empty();

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(emptyRule)
                );

        List<SmartAlert> alerts =
                engine.generate(createSnapshot());

        assertTrue(alerts.isEmpty());
    }

    @Test
    void shouldKeepHighestPriorityFromSameTopic() {
        SmartAlertRule informationalRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.information",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.INFORMATIONAL
                        )
                );

        SmartAlertRule criticalRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.critical",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.CRITICAL
                        )
                );

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(
                                informationalRule,
                                criticalRule
                        )
                );

        List<SmartAlert> alerts =
                engine.generate(createSnapshot());

        assertEquals(1, alerts.size());

        assertEquals(
                "budget.critical",
                alerts.getFirst().code()
        );
    }

    @Test
    void shouldKeepFirstAlertWhenPrioritiesAreEqual() {
        SmartAlertRule firstRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.first",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.ATTENTION
                        )
                );

        SmartAlertRule secondRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.second",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.ATTENTION
                        )
                );

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(
                                firstRule,
                                secondRule
                        )
                );

        List<SmartAlert> alerts =
                engine.generate(createSnapshot());

        assertEquals(1, alerts.size());

        assertEquals(
                "budget.first",
                alerts.getFirst().code()
        );
    }

    @Test
    void shouldOrderAlertsByPriority() {
        SmartAlertRule informationRule = snapshot ->
                Optional.of(
                        createAlert(
                                "goal.information",
                                SmartAlertTopic
                                        .FINANCIAL_GOAL_PROGRESS,
                                SmartAlertPriority.INFORMATIONAL
                        )
                );

        SmartAlertRule criticalRule = snapshot ->
                Optional.of(
                        createAlert(
                                "budget.critical",
                                SmartAlertTopic.BUDGET_USAGE,
                                SmartAlertPriority.CRITICAL
                        )
                );

        SmartAlertRule attentionRule = snapshot ->
                Optional.of(
                        createAlert(
                                "pending.attention",
                                SmartAlertTopic.PENDING_DECISIONS,
                                SmartAlertPriority.ATTENTION
                        )
                );

        SmartAlertEngine engine =
                new SmartAlertEngine(
                        List.of(
                                informationRule,
                                criticalRule,
                                attentionRule
                        )
                );

        List<SmartAlert> alerts =
                engine.generate(createSnapshot());

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alerts.get(0).priority()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alerts.get(1).priority()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alerts.get(2).priority()
        );
    }

    @Test
    void shouldRejectNullRuleList() {
        assertThrows(
                NullPointerException.class,
                () -> new SmartAlertEngine(null)
        );
    }

    @Test
    void shouldRejectNullRuleInsideList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlertEngine(
                        java.util.Arrays.asList(
                                snapshot -> Optional.empty(),
                                null
                        )
                )
        );
    }

    @Test
    void shouldRejectMissingSnapshot() {
        SmartAlertEngine engine =
                new SmartAlertEngine(List.of());

        assertThrows(
                NullPointerException.class,
                () -> engine.generate(null)
        );
    }

    private SmartAlert createAlert(
            String code,
            SmartAlertTopic topic,
            SmartAlertPriority priority
    ) {
        return new SmartAlert(
                code,
                topic,
                priority,
                "Título do alerta",
                "Mensagem do alerta."
        );
    }

    private SmartAlertSnapshot createSnapshot() {
        return new SmartAlertSnapshot(
                new BigDecimal("50"),
                new BigDecimal("500"),
                new BigDecimal("200"),
                new BigDecimal("500"),
                new BigDecimal("30"),
                new BigDecimal("25"),
                3,
                2,
                1,
                0,
                300,
                new BigDecimal("1000")
        );
    }
}