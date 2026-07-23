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

class PreservedValueAlertRuleTest {

    private final PreservedValueAlertRule rule =
            new PreservedValueAlertRule();

    @Test
    void shouldNotGenerateAlertWithoutPreservedValue() {
        Optional<SmartAlert> alert = rule.evaluate(
                snapshot("0")
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldGenerateAlertForPreservedValue() {
        SmartAlert alert = rule.evaluate(
                snapshot("350")
        ).orElseThrow();

        assertEquals(
                "preserved-value.year-total",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.PRESERVED_VALUE,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.INFORMATIONAL,
                alert.priority()
        );

        assertEquals(
                "Você evitou R$ 350,00 em compras neste ano.",
                alert.message()
        );
    }

    @Test
    void shouldFormatThousandsUsingBrazilianCurrency() {
        SmartAlert alert = rule.evaluate(
                snapshot("2350")
        ).orElseThrow();

        assertEquals(
                "Você evitou R$ 2.350,00 em compras neste ano.",
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
            String preservedAmount
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
                0,
                new BigDecimal(preservedAmount)
        );
    }
}