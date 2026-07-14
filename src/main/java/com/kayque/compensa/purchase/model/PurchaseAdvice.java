package com.kayque.compensa.purchase.model;

import java.util.List;
import java.util.Objects;

public record PurchaseAdvice(
        PurchaseDecisionStatus status,
        int score,
        List<String> reasons
) {

    public PurchaseAdvice {
        Objects.requireNonNull(
                status,
                "O status da decisão é obrigatório."
        );

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "A pontuação deve estar entre 0 e 100."
            );
        }

        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "A análise precisa apresentar pelo menos um motivo."
            );
        }

        reasons = List.copyOf(reasons);
    }
}