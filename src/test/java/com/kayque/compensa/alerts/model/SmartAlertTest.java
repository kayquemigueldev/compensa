package com.kayque.compensa.alerts.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertTest {

    @Test
    void shouldCreateSmartAlert() {
        SmartAlert alert = new SmartAlert(
                "budget.almost.exhausted",
                SmartAlertTopic.BUDGET_USAGE,
                SmartAlertPriority.CRITICAL,
                "Orçamento quase esgotado",
                "Você já utilizou 92% do orçamento deste mês."
        );

        assertEquals(
                "budget.almost.exhausted",
                alert.code()
        );

        assertEquals(
                SmartAlertTopic.BUDGET_USAGE,
                alert.topic()
        );

        assertEquals(
                SmartAlertPriority.CRITICAL,
                alert.priority()
        );

        assertEquals(
                "Orçamento quase esgotado",
                alert.title()
        );
    }

    @Test
    void shouldTrimTextValues() {
        SmartAlert alert = new SmartAlert(
                "  budget.warning  ",
                SmartAlertTopic.BUDGET_USAGE,
                SmartAlertPriority.ATTENTION,
                "  Atenção ao orçamento  ",
                "  Seu orçamento exige atenção.  "
        );

        assertEquals(
                "budget.warning",
                alert.code()
        );

        assertEquals(
                "Atenção ao orçamento",
                alert.title()
        );

        assertEquals(
                "Seu orçamento exige atenção.",
                alert.message()
        );
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlert(
                        " ",
                        SmartAlertTopic.BUDGET_USAGE,
                        SmartAlertPriority.ATTENTION,
                        "Atenção",
                        "Mensagem"
                )
        );
    }

    @Test
    void shouldRejectMissingTopic() {
        assertThrows(
                NullPointerException.class,
                () -> new SmartAlert(
                        "budget.warning",
                        null,
                        SmartAlertPriority.ATTENTION,
                        "Atenção",
                        "Mensagem"
                )
        );
    }

    @Test
    void shouldRejectMissingPriority() {
        assertThrows(
                NullPointerException.class,
                () -> new SmartAlert(
                        "budget.warning",
                        SmartAlertTopic.BUDGET_USAGE,
                        null,
                        "Atenção",
                        "Mensagem"
                )
        );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlert(
                        "budget.warning",
                        SmartAlertTopic.BUDGET_USAGE,
                        SmartAlertPriority.ATTENTION,
                        "",
                        "Mensagem"
                )
        );
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlert(
                        "budget.warning",
                        SmartAlertTopic.BUDGET_USAGE,
                        SmartAlertPriority.ATTENTION,
                        "Atenção",
                        " "
                )
        );
    }

    @Test
    void shouldCompareAlertPriorities() {
        assertTrue(
                SmartAlertPriority.CRITICAL.isHigherThan(
                        SmartAlertPriority.ATTENTION
                )
        );

        assertTrue(
                SmartAlertPriority.ATTENTION.isHigherThan(
                        SmartAlertPriority.INFORMATIONAL
                )
        );

        assertFalse(
                SmartAlertPriority.INFORMATIONAL.isHigherThan(
                        SmartAlertPriority.CRITICAL
                )
        );
    }

    @Test
    void shouldConsiderPriorityHigherThanMissingValue() {
        assertTrue(
                SmartAlertPriority.INFORMATIONAL
                        .isHigherThan(null)
        );
    }
}