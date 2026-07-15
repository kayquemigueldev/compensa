package com.kayque.compensa.insights.model;

public record InsightReport(
        int purchaseRate,
        int declineRate,
        int waitingRate,
        long averageRealWorkMinutes,
        int satisfactionRate,
        int regretRate,
        long evaluatedPurchases,
        String headline,
        String description
) {

    public InsightReport {
        validatePercentage(purchaseRate);
        validatePercentage(declineRate);
        validatePercentage(waitingRate);
        validatePercentage(satisfactionRate);
        validatePercentage(regretRate);

        if (averageRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "A média de tempo não pode ser negativa."
            );
        }

        if (evaluatedPurchases < 0) {
            throw new IllegalArgumentException(
                    "O número de avaliações não pode ser negativo."
            );
        }

        if (headline == null || headline.isBlank()) {
            throw new IllegalArgumentException(
                    "O título do insight é obrigatório."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do insight é obrigatória."
            );
        }
    }

    public InsightReport(
            int purchaseRate,
            int declineRate,
            int waitingRate,
            long averageRealWorkMinutes,
            String headline,
            String description
    ) {
        this(
                purchaseRate,
                declineRate,
                waitingRate,
                averageRealWorkMinutes,
                0,
                0,
                0,
                headline,
                description
        );
    }

    private static void validatePercentage(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    "A porcentagem deve estar entre 0 e 100."
            );
        }
    }
}