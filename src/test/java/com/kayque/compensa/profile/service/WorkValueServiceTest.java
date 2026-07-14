package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkValueServiceTest {

    private final WorkValueService service = new WorkValueService();

    @Test
    void shouldCalculateProfessionalAndRealHourlyValues() {
        FinancialProfile profile = new FinancialProfile(
                new BigDecimal("2000.00"),
                new BigDecimal("160"),
                new BigDecimal("40")
        );

        BigDecimal professionalHourlyValue =
                service.calculateProfessionalHourlyValue(profile);

        BigDecimal realHourlyValue =
                service.calculateRealHourlyValue(profile);

        assertEquals(
                new BigDecimal("12.50"),
                professionalHourlyValue
        );

        assertEquals(
                new BigDecimal("10.00"),
                realHourlyValue
        );
    }
}