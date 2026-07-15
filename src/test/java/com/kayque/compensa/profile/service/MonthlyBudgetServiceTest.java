package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlyBudgetServiceTest {

    private final MonthlyBudgetService service =
            new MonthlyBudgetService();

    @Test
    void shouldCalculateAvailableMonthlyAmount() {
        FinancialProfile profile = createProfile(
                "2000.00",
                "1200.00",
                "300.00"
        );

        MonthlyBudgetSummary summary =
                service.calculate(profile);

        assertEquals(
                new BigDecimal("500.00"),
                summary.availableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.AVAILABLE,
                summary.status()
        );
    }

    @Test
    void shouldIdentifyBalancedBudget() {
        FinancialProfile profile = createProfile(
                "2000.00",
                "1500.00",
                "500.00"
        );

        MonthlyBudgetSummary summary =
                service.calculate(profile);

        assertEquals(
                new BigDecimal("0.00"),
                summary.availableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.BALANCED,
                summary.status()
        );
    }

    @Test
    void shouldIdentifyMonthlyDeficit() {
        FinancialProfile profile = createProfile(
                "2000.00",
                "1800.00",
                "400.00"
        );

        MonthlyBudgetSummary summary =
                service.calculate(profile);

        assertEquals(
                new BigDecimal("-200.00"),
                summary.availableAmount()
        );

        assertEquals(
                MonthlyBudgetStatus.DEFICIT,
                summary.status()
        );
    }

    private FinancialProfile createProfile(
            String income,
            String essentialExpenses,
            String savingsGoal
    ) {
        return new FinancialProfile(
                new BigDecimal(income),
                new BigDecimal("160"),
                new BigDecimal("40"),
                new BigDecimal(essentialExpenses),
                new BigDecimal(savingsGoal)
        );
    }
}