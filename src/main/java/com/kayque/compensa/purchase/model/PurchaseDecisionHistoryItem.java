package com.kayque.compensa.purchase.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record PurchaseDecisionHistoryItem(
        long id,
        String productName,
        BigDecimal price,
        long realWorkMinutes,
        PurchaseDecisionStatus adviceStatus,
        PurchaseDecisionOutcome outcome,
        PurchaseSatisfaction satisfaction,
        Instant createdAt
) {

    public PurchaseDecisionHistoryItem {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O ID da decisão deve ser positivo."
            );
        }

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do produto é obrigatório."
            );
        }

        if (price == null
                || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O preço deve ser maior que zero."
            );
        }

        if (realWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "O tempo real não pode ser negativo."
            );
        }

        Objects.requireNonNull(
                adviceStatus,
                "A recomendação é obrigatória."
        );

        Objects.requireNonNull(
                outcome,
                "A decisão tomada é obrigatória."
        );

        Objects.requireNonNull(
                createdAt,
                "A data da decisão é obrigatória."
        );
    }
}