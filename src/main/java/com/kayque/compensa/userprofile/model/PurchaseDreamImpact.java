package com.kayque.compensa.userprofile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PurchaseDreamImpact(
        String dreamName,
        BigDecimal targetAmount,
        BigDecimal purchaseAmount,
        BigDecimal targetPercentage
) {

    public PurchaseDreamImpact {
        if (dreamName == null || dreamName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do objetivo é obrigatório."
            );
        }

        dreamName = dreamName.trim();

        requirePositive(
                targetAmount,
                "O valor do objetivo deve ser positivo."
        );

        requirePositive(
                purchaseAmount,
                "O valor da compra deve ser positivo."
        );

        Objects.requireNonNull(
                targetPercentage,
                "A porcentagem do objetivo é obrigatória."
        );

        if (targetPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "A porcentagem não pode ser negativa."
            );
        }
    }

    private static void requirePositive(
            BigDecimal value,
            String message
    ) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}