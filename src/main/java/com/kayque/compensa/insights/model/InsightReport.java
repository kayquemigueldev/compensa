package com.kayque.compensa.insights.model;

public record InsightReport(
        int purchaseRate,
        int declineRate,
        int waitingRate,
        long averageRealWorkMinutes,
        String headline,
        String description
) {

    public InsightReport {
        validatePercentage(purchaseRate);
        validatePercentage(declineRate);
        validatePercentage(waitingRate);

        if (averageRealWorkMinutes < 0) {
            throw new IllegalArgumentException(
                    "A média de tempo não pode ser negativa."
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

    private static void validatePercentage(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    "A porcentagem deve estar entre 0 e 100."
            );
        }
    }
}