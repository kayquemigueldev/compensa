package com.kayque.compensa.purchase.model;

public record PurchaseAdviceMessage(
        String title,
        String description
) {

    public PurchaseAdviceMessage {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "O título da recomendação é obrigatório."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição da recomendação é obrigatória."
            );
        }
    }
}