package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlyBudgetUsageServiceTest {

    private final MonthlyBudgetUsageService service =
            new MonthlyBudgetUsageService();

    @Test
    void shouldSubtractPurchasedAmountFromPlannedBudget() {
        MonthlyBudgetUsage usage =
                service.calculate(
                        createPlannedBudget("500.00"),
                        new BigDecimal("107.99")
                );

        assertEquals(
                new BigDecimal("392.01"),
                usage.currentAvailableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.AVAILABLE,
                usage.status()
        );
    }

    @Test
    void shouldIdentifyBudgetExhaustion() {
        MonthlyBudgetUsage usage =
                service.calculate(
                        createPlannedBudget("500.00"),
                        new BigDecimal("500.00")
                );

        assertEquals(
                new BigDecimal("0.00"),
                usage.currentAvailableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.BALANCED,
                usage.status()
        );
    }

    @Test
    void shouldIdentifyDeficitCausedByPurchases() {
        MonthlyBudgetUsage usage =
                service.calculate(
                        createPlannedBudget("500.00"),
                        new BigDecimal("650.00")
                );

        assertEquals(
                new BigDecimal("-150.00"),
                usage.currentAvailableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.DEFICIT,
                usage.status()
        );
    }

    private MonthlyBudgetSummary createPlannedBudget(
            String availableAmount
    ) {
        return new MonthlyBudgetSummary(
                new BigDecimal("2000.00"),
                new BigDecimal("1200.00"),
                new BigDecimal("300.00"),
                new BigDecimal(availableAmount),
                MonthlyBudgetStatus.AVAILABLE
        );
    }
}