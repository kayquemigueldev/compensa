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

class BudgetUsageAlertRuleTest {

    private final BudgetUsageAlertRule rule =
            new BudgetUsageAlertRule();

    @Test
    void shouldGenerateCriticalAlertForBudgetDeficit() {
        SmartAlert alert = rule.evaluate(
                snapshot("80", "-25")
        ).orElseThrow();

        assertEquals(
                "budget.deficit",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );

        assertEquals(
                SmartAlertTopic.BUDGET_USAGE,
                alert.topic()
        );
    }

    @Test
    void shouldGenerateCriticalAlertWhenBudgetIsExhausted() {
        SmartAlert alert = rule.evaluate(
                snapshot("100", "0")
        ).orElseThrow();

        assertEquals(
                "budget.exhausted",
                alert.code()
        );

        assertEquals(
                "Você já utilizou 100% do orçamento deste mês.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateCriticalAlertWhenBudgetIsAlmostExhausted() {
        SmartAlert alert = rule.evaluate(
                snapshot("92", "40")
        ).orElseThrow();

        assertEquals(
                "budget.almost-exhausted",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );
    }

    @Test
    void shouldGenerateAttentionAlertForHighBudgetUsage() {
        SmartAlert alert = rule.evaluate(
                snapshot("82", "120")
        ).orElseThrow();

        assertEquals(
                "budget.attention",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "Você já utilizou 82% do orçamento deste mês.",
                alert.message()
        );
    }

    @Test
    void shouldRespectAttentionBoundary() {
        SmartAlert alert = rule.evaluate(
                snapshot("75", "250")
        ).orElseThrow();

        assertEquals(
                "budget.attention",
                alert.code()
        );
    }

    @Test
    void shouldGenerateInformationalAlertForHealthyUsage() {
        SmartAlert alert = rule.evaluate(
                snapshot("50", "500")
        ).orElseThrow();

        assertEquals(
                "budget.healthy",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );
    }

    @Test
    void shouldNotGenerateAlertForIntermediateUsage() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("60", "400")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldFormatDecimalPercentageUsingComma() {
        SmartAlert alert = rule.evaluate(
                snapshot("82.5", "120")
        ).orElseThrow();

        assertEquals(
                "Você já utilizou 82,5% do orçamento deste mês.",
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
            String usagePercentage,
            String availableBudget
    ) {
        return new SmartAlertSnapshot(
                new BigDecimal(usagePercentage),
                new BigDecimal(availableBudget),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO
        );
    }
}