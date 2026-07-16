package com.kayque.compensa.userprofile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record UserProfile(
        String displayName,
        UserGoal mainGoal,
        RecommendationTone recommendationTone,
        String currentDream,
        BigDecimal currentDreamTargetAmount,
        BigDecimal currentDreamSavedAmount
) {

    public UserProfile(
            String displayName,
            UserGoal mainGoal,
            RecommendationTone recommendationTone,
            String currentDream
    ) {
        this(
                displayName,
                mainGoal,
                recommendationTone,
                currentDream,
                null,
                BigDecimal.ZERO
        );
    }

    public UserProfile(
            String displayName,
            UserGoal mainGoal,
            RecommendationTone recommendationTone,
            String currentDream,
            BigDecimal currentDreamTargetAmount
    ) {
        this(
                displayName,
                mainGoal,
                recommendationTone,
                currentDream,
                currentDreamTargetAmount,
                BigDecimal.ZERO
        );
    }

    public UserProfile {
        if (displayName == null
                || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe como devemos chamar você."
            );
        }

        displayName = displayName.trim();

        if (displayName.length() > 50) {
            throw new IllegalArgumentException(
                    "O nome deve possuir no máximo 50 caracteres."
            );
        }

        Objects.requireNonNull(
                mainGoal,
                "Selecione seu principal objetivo."
        );

        Objects.requireNonNull(
                recommendationTone,
                "Selecione como prefere receber recomendações."
        );

        currentDream = currentDream == null
                ? ""
                : currentDream.trim();

        if (currentDream.length() > 120) {
            throw new IllegalArgumentException(
                    "O objetivo atual deve possuir no máximo 120 caracteres."
            );
        }

        if (currentDreamTargetAmount != null
                && currentDreamTargetAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new IllegalArgumentException(
                    "O valor do objetivo deve ser maior que zero."
            );
        }

        currentDreamSavedAmount =
                currentDreamSavedAmount == null
                        ? BigDecimal.ZERO
                        : currentDreamSavedAmount;

        if (currentDreamSavedAmount.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "O valor já guardado não pode ser negativo."
            );
        }

        if (currentDream.isBlank()
                && currentDreamTargetAmount != null) {
            throw new IllegalArgumentException(
                    "Informe qual é o objetivo antes de definir seu valor."
            );
        }

        if (currentDream.isBlank()
                && currentDreamSavedAmount.compareTo(
                BigDecimal.ZERO
        ) > 0) {
            throw new IllegalArgumentException(
                    "Informe qual é o objetivo antes de registrar o valor guardado."
            );
        }

        if (currentDreamTargetAmount == null
                && currentDreamSavedAmount.compareTo(
                BigDecimal.ZERO
        ) > 0) {
            throw new IllegalArgumentException(
                    "Informe o valor do objetivo antes de registrar quanto já foi guardado."
            );
        }
    }

    public boolean hasCurrentDream() {
        return !currentDream.isBlank();
    }

    public boolean hasCurrentDreamTargetAmount() {
        return currentDreamTargetAmount != null;
    }

    public boolean hasCurrentDreamSavedAmount() {
        return currentDreamSavedAmount.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    public boolean isCurrentDreamCompleted() {
        return currentDreamTargetAmount != null
                && currentDreamSavedAmount.compareTo(
                currentDreamTargetAmount
        ) >= 0;
    }
}