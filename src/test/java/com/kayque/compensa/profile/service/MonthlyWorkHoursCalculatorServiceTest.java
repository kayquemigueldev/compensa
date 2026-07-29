package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.MonthlyWorkHoursCalculation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyWorkHoursCalculatorServiceTest {

    private final MonthlyWorkHoursCalculatorService service =
            new MonthlyWorkHoursCalculatorService();

    @Test
    void shouldCalculateMonthlyProfessionalHours() {
        MonthlyWorkHoursCalculation result =
                service.calculate(
                        new BigDecimal("8"),
                        5,
                        BigDecimal.ZERO
                );

        assertEquals(
                new BigDecimal("173.33"),
                result.monthlyProfessionalHours()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.monthlyAdditionalHours()
        );

        assertEquals(
                new BigDecimal("173.33"),
                result.monthlyCommittedHours()
        );
    }

    @Test
    void shouldIncludeAdditionalDailyTime() {
        MonthlyWorkHoursCalculation result =
                service.calculate(
                        new BigDecimal("8"),
                        5,
                        new BigDecimal("1")
                );

        assertEquals(
                new BigDecimal("173.33"),
                result.monthlyProfessionalHours()
        );

        assertEquals(
                new BigDecimal("21.67"),
                result.monthlyAdditionalHours()
        );

        assertEquals(
                new BigDecimal("195.00"),
                result.monthlyCommittedHours()
        );
    }

    @Test
    void shouldAcceptDecimalDailyHours() {
        MonthlyWorkHoursCalculation result =
                service.calculate(
                        new BigDecimal("7.5"),
                        5,
                        new BigDecimal("0.5")
                );

        assertEquals(
                new BigDecimal("162.50"),
                result.monthlyProfessionalHours()
        );

        assertEquals(
                new BigDecimal("10.83"),
                result.monthlyAdditionalHours()
        );

        assertEquals(
                new BigDecimal("173.33"),
                result.monthlyCommittedHours()
        );
    }

    @Test
    void shouldRejectZeroProfessionalHours() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        BigDecimal.ZERO,
                        5,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectInvalidWorkDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        new BigDecimal("8"),
                        0,
                        BigDecimal.ZERO
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        new BigDecimal("8"),
                        8,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeAdditionalHours() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        new BigDecimal("8"),
                        5,
                        new BigDecimal("-1")
                )
        );
    }

    @Test
    void shouldRejectMoreThanTwentyFourCommittedHours() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        new BigDecimal("20"),
                        5,
                        new BigDecimal("5")
                )
        );
    }
}