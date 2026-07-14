package com.kayque.compensa.purchase.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Purchase(
        String name,
        BigDecimal price,
        PurchaseFrequency frequency
) {

    public Purchase {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da compra é obrigatório."
            );
        }

        if (price == null
                || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O preço deve ser maior que zero."
            );
        }

        Objects.requireNonNull(
                frequency,
                "A frequência da compra é obrigatória."
        );

        name = name.trim();
    }
}