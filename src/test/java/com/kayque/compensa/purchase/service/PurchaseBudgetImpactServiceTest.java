package com.kayque.compensa.purchase.service;

import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpactStatus;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseBudgetImpactServiceTest {

    private final PurchaseBudgetImpactService service =
            new PurchaseBudgetImpactService();

    @Test
    void shouldCalculatePurchasePercentageOfAvailableBudget() {
        PurchaseBudgetImpact impact =
                service.calculate(
                        createPurchase("7.99"),
                        createBudget(
                                "500.00",
                                MonthlyBudgetStatus.AVAILABLE
                        )
                );

        assertEquals(
                Optional.of(new BigDecimal("1.60")),
                impact.budgetUsagePercentage()
        );

        assertEquals(
                new BigDecimal("492.01"),
                impact.remainingAfterPurchase()
        );

        assertEquals(
                PurchaseBudgetImpactStatus.WITHIN_BUDGET,
                impact.status()
        );
    }

    @Test
    void shouldIdentifyPurchaseAboveAvailableBudget() {
        PurchaseBudgetImpact impact =
                service.calculate(
                        createPurchase("600.00"),
                        createBudget(
                                "500.00",
                                MonthlyBudgetStatus.AVAILABLE
                        )
                );

        assertEquals(
                Optional.of(new BigDecimal("120.00")),
                impact.budgetUsagePercentage()
        );

        assertEquals(
                PurchaseBudgetImpactStatus
                        .EXCEEDS_AVAILABLE_AMOUNT,
                impact.status()
        );
    }

    @Test
    void shouldAvoidPercentageWhenBudgetIsZero() {
        PurchaseBudgetImpact impact =
                service.calculate(
                        createPurchase("7.99"),
                        createBudget(
                                "0.00",
                                MonthlyBudgetStatus.BALANCED
                        )
                );

        assertEquals(
                Optional.empty(),
                impact.budgetUsagePercentage()
        );

        assertEquals(
                PurchaseBudgetImpactStatus
                        .NO_AVAILABLE_BUDGET,
                impact.status()
        );
    }

    @Test
    void shouldIdentifyExistingBudgetDeficit() {
        PurchaseBudgetImpact impact =
                service.calculate(
                        createPurchase("7.99"),
                        createBudget(
                                "-200.00",
                                MonthlyBudgetStatus.DEFICIT
                        )
                );

        assertEquals(
                Optional.empty(),
                impact.budgetUsagePercentage()
        );

        assertEquals(
                new BigDecimal("-207.99"),
                impact.remainingAfterPurchase()
        );

        assertEquals(
                PurchaseBudgetImpactStatus
                        .BUDGET_IN_DEFICIT,
                impact.status()
        );
    }

    private Purchase createPurchase(String price) {
        return new Purchase(
                "Compra de teste",
                new BigDecimal(price),
                PurchaseFrequency.ONCE
        );
    }

    private MonthlyBudgetSummary createBudget(
            String availableAmount,
            MonthlyBudgetStatus status
    ) {
        return new MonthlyBudgetSummary(
                new BigDecimal("2000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal(availableAmount),
                status
        );
    }
}