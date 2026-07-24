package com.kayque.compensa.purchase.model;

import com.kayque.compensa.purchase.score.model.PurchaseScoreClassification;

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
        Integer compensaScore,
        PurchaseScoreClassification scoreClassification,
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

        productName = productName.trim();

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

        boolean onlyScoreIsPresent =
                compensaScore != null
                        && scoreClassification == null;

        boolean onlyClassificationIsPresent =
                compensaScore == null
                        && scoreClassification != null;

        if (onlyScoreIsPresent
                || onlyClassificationIsPresent) {
            throw new IllegalArgumentException(
                    "O valor e a classificação do Score Compensa? devem ser informados juntos."
            );
        }

        if (compensaScore != null) {
            if (compensaScore < 0 || compensaScore > 100) {
                throw new IllegalArgumentException(
                        "O Score Compensa? deve estar entre 0 e 100."
                );
            }

            PurchaseScoreClassification expected =
                    PurchaseScoreClassification.fromScore(
                            compensaScore
                    );

            if (scoreClassification != expected) {
                throw new IllegalArgumentException(
                        "A classificação não corresponde ao Score Compensa?."
                );
            }
        }
    }

    /*
     * Compatibilidade com registros e testes anteriores
     * à persistência do Score Compensa?.
     */
    public PurchaseDecisionHistoryItem(
            long id,
            String productName,
            BigDecimal price,
            long realWorkMinutes,
            PurchaseDecisionStatus adviceStatus,
            PurchaseDecisionOutcome outcome,
            PurchaseSatisfaction satisfaction,
            Instant createdAt
    ) {
        this(
                id,
                productName,
                price,
                realWorkMinutes,
                adviceStatus,
                outcome,
                satisfaction,
                null,
                null,
                createdAt
        );
    }

    public boolean hasCompensaScore() {
        return compensaScore != null;
    }
}