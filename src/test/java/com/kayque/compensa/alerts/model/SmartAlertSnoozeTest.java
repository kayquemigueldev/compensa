package com.kayque.compensa.alerts.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartAlertSnoozeTest {

    @Test
    void shouldBeActiveBeforeExpiration() {
        SmartAlertSnooze snooze =
                new SmartAlertSnooze(
                        "budget-warning",
                        Instant.parse(
                                "2026-07-27T15:00:00Z"
                        )
                );

        assertTrue(
                snooze.isActiveAt(
                        Instant.parse(
                                "2026-07-26T15:00:00Z"
                        )
                )
        );
    }

    @Test
    void shouldNotBeActiveAtExpiration() {
        Instant expiration =
                Instant.parse(
                        "2026-07-27T15:00:00Z"
                );

        SmartAlertSnooze snooze =
                new SmartAlertSnooze(
                        "budget-warning",
                        expiration
                );

        assertFalse(snooze.isActiveAt(expiration));
    }

    @Test
    void shouldRejectBlankAlertCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SmartAlertSnooze(
                        " ",
                        Instant.parse(
                                "2026-07-27T15:00:00Z"
                        )
                )
        );
    }
}