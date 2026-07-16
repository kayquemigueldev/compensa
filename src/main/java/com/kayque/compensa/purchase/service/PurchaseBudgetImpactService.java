package com.kayque.compensa.purchase.service;

import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpact;
import com.kayque.compensa.purchase.model.PurchaseBudgetImpactStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class PurchaseBudgetImpactService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final int PERCENTAGE_SCALE = 2;

    public PurchaseBudgetImpact calculate(
            Purchase purchase,
            MonthlyBudgetSummary budget
    ) {
        Objects.requireNonNull(
                budget,
                "O orçamento mensal é obrigatório."
        );

        return calculate(
                purchase,
                budget.availableAmount()
        );
    }

    public PurchaseBudgetImpact calculate(
            Purchase purchase,
            MonthlyBudgetUsage budgetUsage
    ) {
        Objects.requireNonNull(
                budgetUsage,
                "O uso do orçamento mensal é obrigatório."
        );

        return calculate(
                purchase,
                budgetUsage.currentAvailableAmount()
        );
    }

    private PurchaseBudgetImpact calculate(
            Purchase purchase,
            BigDecimal availableAmount
    ) {
        Objects.requireNonNull(
                purchase,
                "A compra é obrigatória."
        );

        Objects.requireNonNull(
                availableAmount,
                "O dinheiro disponível é obrigatório."
        );

        BigDecimal remainingAfterPurchase =
                availableAmount.subtract(purchase.price());

        int budgetComparison =
                availableAmount.compareTo(BigDecimal.ZERO);

        if (budgetComparison < 0) {
            return createWithoutPercentage(
                    availableAmount,
                    remainingAfterPurchase,
                    PurchaseBudgetImpactStatus.BUDGET_IN_DEFICIT
            );
        }

        if (budgetComparison == 0) {
            return createWithoutPercentage(
                    availableAmount,
                    remainingAfterPurchase,
                    PurchaseBudgetImpactStatus.NO_AVAILABLE_BUDGET
            );
        }

        BigDecimal percentage =
                calculatePercentage(
                        purchase.price(),
                        availableAmount
                );

        PurchaseBudgetImpactStatus status =
                purchase.price().compareTo(availableAmount) <= 0
                        ? PurchaseBudgetImpactStatus.WITHIN_BUDGET
                        : PurchaseBudgetImpactStatus.EXCEEDS_AVAILABLE_AMOUNT;

        return new PurchaseBudgetImpact(
                availableAmount,
                remainingAfterPurchase,
                Optional.of(percentage),
                status
        );
    }

    private PurchaseBudgetImpact createWithoutPercentage(
            BigDecimal availableAmount,
            BigDecimal remainingAfterPurchase,
            PurchaseBudgetImpactStatus status
    ) {
        return new PurchaseBudgetImpact(
                availableAmount,
                remainingAfterPurchase,
                Optional.empty(),
                status
        );
    }

    private BigDecimal calculatePercentage(
            BigDecimal price,
            BigDecimal availableAmount
    ) {
        return price
                .multiply(ONE_HUNDRED)
                .divide(
                        availableAmount,
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}