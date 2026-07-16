package com.kayque.compensa.userprofile.model;

import java.math.BigDecimal;
import java.util.Objects;



public record UserProfile(
        String displayName,
        UserGoal mainGoal,
        RecommendationTone recommendationTone,
        String currentDream,
        BigDecimal currentDreamTargetAmount
) {

    private static final int MAXIMUM_NAME_LENGTH = 50;
    private static final int MAXIMUM_DREAM_LENGTH = 120;

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
                null
        );
    }

    public UserProfile {
        if (displayName == null
                || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome ou apelido é obrigatório."
            );
        }

        displayName = displayName.trim();

        if (displayName.length()
                > MAXIMUM_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "O nome deve possuir no máximo 50 caracteres."
            );
        }

        Objects.requireNonNull(
                mainGoal,
                "O objetivo principal é obrigatório."
        );

        Objects.requireNonNull(
                recommendationTone,
                "O tom das recomendações é obrigatório."
        );

        currentDream = currentDream == null
                ? ""
                : currentDream.trim();

        if (currentDream.length()
                > MAXIMUM_DREAM_LENGTH) {
            throw new IllegalArgumentException(
                    "O objetivo atual deve possuir no máximo 120 caracteres."
            );
        }

        if (currentDreamTargetAmount != null
                && currentDreamTargetAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "O valor do objetivo deve ser maior que zero."
            );
        }

        if (currentDream.isBlank()
                && currentDreamTargetAmount != null) {
            throw new IllegalArgumentException(
                    "Informe o objetivo antes de definir seu valor."
            );
        }
    }

    public boolean hasCurrentDream() {
        return !currentDream.isBlank();
    }

    public boolean hasCurrentDreamTargetAmount() {
        return currentDreamTargetAmount != null;
    }
}