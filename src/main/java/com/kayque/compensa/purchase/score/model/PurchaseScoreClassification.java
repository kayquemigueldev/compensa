package com.kayque.compensa.purchase.score.model;

public enum PurchaseScoreClassification {

    EXCELLENT(
            "Excelente decisão",
            PurchaseScoreColor.GREEN,
            85
    ),

    GOOD(
            "Boa decisão",
            PurchaseScoreColor.GREEN,
            70
    ),

    MODERATE(
            "Decisão moderada",
            PurchaseScoreColor.YELLOW,
            50
    ),

    RISKY(
            "Decisão arriscada",
            PurchaseScoreColor.ORANGE,
            30
    ),

    CRITICAL(
            "Decisão crítica",
            PurchaseScoreColor.RED,
            0
    );

    private final String description;
    private final PurchaseScoreColor color;
    private final int minimumScore;

    PurchaseScoreClassification(
            String description,
            PurchaseScoreColor color,
            int minimumScore
    ) {
        this.description = description;
        this.color = color;
        this.minimumScore = minimumScore;
    }

    public String getDescription() {
        return description;
    }

    public PurchaseScoreColor getColor() {
        return color;
    }

    public int getMinimumScore() {
        return minimumScore;
    }

    public static PurchaseScoreClassification fromScore(
            int score
    ) {
        validateScore(score);

        for (
                PurchaseScoreClassification classification
                : values()
        ) {
            if (score >= classification.minimumScore) {
                return classification;
            }
        }

        throw new IllegalStateException(
                "Não foi possível classificar o Score Compensa?."
        );
    }

    private static void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "O Score Compensa? deve estar entre 0 e 100."
            );
        }
    }
}