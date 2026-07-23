package com.kayque.compensa.alerts.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalExpectedProgressServiceTest {

    private final GoalExpectedProgressService service =
            new GoalExpectedProgressService();

    @Test
    void shouldCalculateHalfOfExpectedProgress() {
        BigDecimal result = service.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                LocalDate.of(2026, 1, 6)
        );

        assertEquals(
                new BigDecimal("50.00"),
                result
        );
    }

    @Test
    void shouldReturnZeroBeforeGoalCreation() {
        BigDecimal result = service.calculate(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 1, 20)
        );

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldReturnOneHundredOnTargetDate() {
        BigDecimal result = service.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertEquals(
                new BigDecimal("100"),
                result
        );
    }

    @Test
    void shouldReturnOneHundredAfterTargetDate() {
        BigDecimal result = service.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 8, 1)
        );

        assertEquals(
                new BigDecimal("100"),
                result
        );
    }

    @Test
    void shouldRejectTargetBeforeCreation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 15)
                )
        );
    }
}