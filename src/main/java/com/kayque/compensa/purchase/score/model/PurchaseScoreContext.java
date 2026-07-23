package com.kayque.compensa.purchase.score.model;

import com.kayque.compensa.purchase.model.PurchaseFrequency;
import com.kayque.compensa.purchase.model.PurchaseMotivation;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record PurchaseScoreContext(
        PurchaseMotivation motivation,
        PurchaseFrequency frequency,
        boolean planned,
        boolean urgent,
        boolean hasAlternative,
        long professionalWorkMinutes,
        long realWorkMinutes,
        BigDecimal projectedAnnualCost,
        BigDecimal netMonthlyIncome,
        BigDecimal budgetImpactPercentage,
        BigDecimal goalImpactPercentage
) {

    public PurchaseScoreContext {
        Objects.requireNonNull(
                motivation,
                "A motivação da compra é obrigatória."
        );

        Objects.requireNonNull(
                frequency,
                "A frequência da compra é obrigatória."
        );

        Objects.requireNonNull(
                projectedAnnualCost,
                "A projeção anual é obrigatória."
        );

        Objects.requireNonNull(
                netMonthlyIncome,
                "A renda mensal é obrigatória."
        );

        if (professionalWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "O tempo profissional não pode ser negativo."
            );
        }

        if (realWorkMinutes < professionalWorkMinutes) {
            throw new IllegalArgumentException(
                    "O tempo real não pode ser menor que o tempo profissional."
            );
        }

        if (
                projectedAnnualCost.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "A projeção anual não pode ser negativa."
            );
        }

        if (
                netMonthlyIncome.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {
            throw new IllegalArgumentException(
                    "A renda mensal deve ser maior que zero."
            );
        }

        validateOptionalPercentage(
                budgetImpactPercentage,
                "O impacto no orçamento"
        );

        validateOptionalPercentage(
                goalImpactPercentage,
                "O impacto no objetivo"
        );
    }

    public Optional<BigDecimal> budgetImpact() {
        return Optional.ofNullable(
                budgetImpactPercentage
        );
    }

    public Optional<BigDecimal> goalImpact() {
        return Optional.ofNullable(
                goalImpactPercentage
        );
    }

    private static void validateOptionalPercentage(
            BigDecimal percentage,
            String fieldName
    ) {
        if (
                percentage != null
                        && percentage.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser negativo."
            );
        }
    }
}