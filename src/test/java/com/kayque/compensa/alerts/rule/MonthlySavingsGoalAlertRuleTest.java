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

class MonthlySavingsGoalAlertRuleTest {

    private final MonthlySavingsGoalAlertRule rule =
            new MonthlySavingsGoalAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutMonthlyGoal() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("0", "0")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateAlertWhenGoalIsReached() {
        SmartAlert alert = rule.evaluate(
                snapshot("500", "500")
        ).orElseThrow();

        assertEquals(
                "monthly-goal.reached",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.MONTHLY_SAVINGS_GOAL,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "Excelente! Você atingiu sua meta mensal de economia.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAlertWhenGoalIsExceeded() {
        SmartAlert alert = rule.evaluate(
                snapshot("650", "500")
        ).orElseThrow();

        assertEquals(
                "monthly-goal.exceeded",
                alert.code()
        );

        assertEquals(
                "Excelente! Você superou sua meta mensal "
                        + "de economia em R$ 150,00.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAlertWhenGoalIsAlmostReached() {
        SmartAlert alert = rule.evaluate(
                snapshot("480", "600")
        ).orElseThrow();

        assertEquals(
                "monthly-goal.almost-reached",
                alert.code()
        );

        assertEquals(
                "Faltam apenas R$ 120,00 para alcançar "
                        + "sua meta deste mês.",
                alert.message()
        );
    }

    @Test
    void shouldRespectTwentyPercentBoundary() {
        SmartAlert alert = rule.evaluate(
                snapshot("800", "1000")
        ).orElseThrow();

        assertEquals(
                "monthly-goal.almost-reached",
                alert.code()
        );

        assertEquals(
                "Faltam apenas R$ 200,00 para alcançar "
                        + "sua meta deste mês.",
                alert.message()
        );
    }

    @Test
    void shouldNotGenerateAlertWhenGoalIsStillDistant() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("790", "1000")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldNotGenerateAlmostReachedAlertWithoutContributions() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("0", "500")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldRejectMissingSnapshot() {
        assertThrows(
                NullPointerException.class,
                () -> rule.evaluate(null)
        );
    }

    private SmartAlertSnapshot snapshot(
            String monthlyContributions,
            String monthlyTarget
    ) {
        return new SmartAlertSnapshot(
                BigDecimal.ZERO,
                new BigDecimal("500"),
                new BigDecimal(monthlyContributions),
                new BigDecimal(monthlyTarget),
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