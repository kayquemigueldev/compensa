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

class WorkTimeAlertRuleTest {

    private final WorkTimeAlertRule rule =
            new WorkTimeAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutWorkTime() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot(0)
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldNotGenerateAlertBelowOneWorkDay() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot(479)
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateInformationalAlertAtOneWorkDay() {
        SmartAlert alert = rule.evaluate(
                snapshot(480)
        ).orElseThrow();

        assertEquals(
                "work-time.informational",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.WORK_TIME,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "As compras analisadas representam 8h do seu trabalho.",
                alert.message()
        );
    }

    @Test
    void shouldFormatHoursAndMinutes() {
        SmartAlert alert = rule.evaluate(
                snapshot(1746)
        ).orElseThrow();

        assertEquals(
                "As compras analisadas representam "
                        + "29h 6min do seu trabalho.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateAttentionAlertAtOneWorkWeek() {
        SmartAlert alert = rule.evaluate(
                snapshot(2400)
        ).orElseThrow();

        assertEquals(
                "work-time.attention",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.ATTENTION,
                alert.priority()
        );

        assertEquals(
                "As compras analisadas representam 40h de trabalho, "
                        + "o equivalente a pelo menos uma semana completa.",
                alert.message()
        );
    }

    @Test
    void shouldGenerateCriticalAlertAtTwoWorkWeeks() {
        SmartAlert alert = rule.evaluate(
                snapshot(4800)
        ).orElseThrow();

        assertEquals(
                "work-time.critical",
                alert.code()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );

        assertEquals(
                "As compras analisadas representam 80h de trabalho. "
                        + "Isso equivale a pelo menos duas semanas "
                        + "completas da sua jornada.",
                alert.message()
        );
    }

    @Test
    void shouldPrioritizeCriticalRange() {
        SmartAlert alert = rule.evaluate(
                snapshot(6000)
        ).orElseThrow();

        assertEquals(
                "work-time.critical",
                alert.code()
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
            long totalWorkMinutes
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
                0,
                0,
                totalWorkMinutes,
                BigDecimal.ZERO
        );
    }
}