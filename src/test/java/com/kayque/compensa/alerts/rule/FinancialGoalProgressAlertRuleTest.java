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

class FinancialGoalProgressAlertRuleTest {

    private final FinancialGoalProgressAlertRule rule =
            new FinancialGoalProgressAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutFinancialGoal() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("0", "0")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateAlertWhenGoalIsCompleted() {
        SmartAlert alert = rule.evaluate(
                snapshot("100", "80")
        ).orElseThrow();

        assertEquals(
                "financial-goal.completed",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.FINANCIAL_GOAL_PROGRESS,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "Parabéns! Você alcançou seu objetivo financeiro.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAlertWhenGoalIsAhead() {
        SmartAlert alert = rule.evaluate(
                snapshot("65", "40")
        ).orElseThrow();

        assertEquals(
                "financial-goal.ahead",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "Seu objetivo financeiro está 25 pontos percentuais "
                        + "à frente do progresso planejado.",
                alert.message()
        );
    }

    @Test
    void shouldRespectAheadBoundary() {
        SmartAlert alert = rule.evaluate(
                snapshot("60", "50")
        ).orElseThrow();

        assertEquals(
                "financial-goal.ahead",
                alert.code()
        );
    }

    @Test
    void shouldGenerateAttentionAlertWhenGoalIsBehind() {
        SmartAlert alert = rule.evaluate(
                snapshot("25", "50")
        ).orElseThrow();

        assertEquals(
                "financial-goal.behind",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "Seu objetivo financeiro está 25 pontos percentuais "
                        + "atrás do progresso planejado.",
                alert.message()
        );
    }

    @Test
    void shouldRespectBehindBoundary() {
        SmartAlert alert = rule.evaluate(
                snapshot("40", "50")
        ).orElseThrow();

        assertEquals(
                "financial-goal.behind",
                alert.code()
        );
    }

    @Test
    void shouldNotGenerateAlertWhenProgressIsCloseToPlan() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("46", "50")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldFormatDecimalDifferenceUsingComma() {
        SmartAlert alert = rule.evaluate(
                snapshot("62.5", "50")
        ).orElseThrow();

        assertEquals(
                "Seu objetivo financeiro está 12,5 pontos percentuais "
                        + "à frente do progresso planejado.",
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
            String currentProgress,
            String expectedProgress
    ) {
        return new SmartAlertSnapshot(
                BigDecimal.ZERO,
                new BigDecimal("500"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal(currentProgress),
                new BigDecimal(expectedProgress),
                0,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO
        );
    }
}