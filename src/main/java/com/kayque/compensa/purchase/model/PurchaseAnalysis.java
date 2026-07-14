package com.kayque.compensa.purchase.model;

import java.math.BigDecimal;

public record PurchaseAnalysis(
        Purchase purchase,
        long professionalWorkMinutes,
        long realWorkMinutes,
        BigDecimal projectedYearlyCost
) {

    public PurchaseAnalysis {
        if (purchase == null) {
            throw new IllegalArgumentException(
                    "A compra analisada é obrigatória."
            );
        }

        if (professionalWorkMinutes < 0 || realWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "O tempo de trabalho não pode ser negativo."
            );
        }

        if (projectedYearlyCost == null
                || projectedYearlyCost.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "O custo projetado não pode ser negativo."
            );
        }
    }
}