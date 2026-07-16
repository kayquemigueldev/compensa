package com.kayque.compensa.userprofile.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;

class UserProfileTest {

    @Test
    void shouldCreateValidProfile() {
        UserProfile profile = new UserProfile(
                "  Kayque  ",
                UserGoal.REDUCE_IMPULSE_PURCHASES,
                RecommendationTone.BALANCED,
                "  Montar meu computador  "
        );

        assertEquals(
                "Kayque",
                profile.displayName()
        );

        assertEquals(
                "Montar meu computador",
                profile.currentDream()
        );

        assertTrue(profile.hasCurrentDream());
    }

    @Test
    void shouldCreateProfileWithDreamTargetAmount() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.PLAN_A_PURCHASE,
                RecommendationTone.DIRECT,
                "Montar meu computador",
                new BigDecimal("5000.00")
        );

        assertTrue(
                profile.hasCurrentDreamTargetAmount()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                profile.currentDreamTargetAmount()
        );
    }

    @Test
    void shouldRejectNonPositiveDreamTargetAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        "Kayque",
                        UserGoal.PLAN_A_PURCHASE,
                        RecommendationTone.BALANCED,
                        "Montar meu computador",
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectTargetAmountWithoutDream() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        "Kayque",
                        UserGoal.SAVE_MONEY,
                        RecommendationTone.BALANCED,
                        "",
                        new BigDecimal("5000.00")
                )
        );
    }

    @Test
    void shouldAllowProfileWithoutCurrentDream() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.SAVE_MONEY,
                RecommendationTone.GENTLE,
                null
        );

        assertEquals("", profile.currentDream());
        assertFalse(profile.hasCurrentDream());
    }

    @Test
    void shouldRejectBlankDisplayName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        " ",
                        UserGoal.ORGANIZE_BUDGET,
                        RecommendationTone.DIRECT,
                        ""
                )
        );
    }

    @Test
    void shouldRejectCurrentDreamAboveLimit() {
        String oversizedDream =
                "A".repeat(121);

        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        "Kayque",
                        UserGoal.PLAN_A_PURCHASE,
                        RecommendationTone.BALANCED,
                        oversizedDream
                )
        );
    }

    @Test
    void shouldCreateProfileWithSavedDreamAmount() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.REDUCE_IMPULSE_PURCHASES,
                RecommendationTone.DIRECT,
                "Montar meu computador",
                new BigDecimal("5000"),
                new BigDecimal("1200")
        );

        assertEquals(
                new BigDecimal("1200"),
                profile.currentDreamSavedAmount()
        );

        assertTrue(profile.hasCurrentDreamSavedAmount());
        assertFalse(profile.isCurrentDreamCompleted());
    }

    @Test
    void shouldRejectNegativeSavedDreamAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        "Kayque",
                        UserGoal.SAVE_MONEY,
                        RecommendationTone.BALANCED,
                        "Montar meu computador",
                        new BigDecimal("5000"),
                        new BigDecimal("-100")
                )
        );
    }

    @Test
    void shouldRejectSavedAmountWithoutTargetAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserProfile(
                        "Kayque",
                        UserGoal.SAVE_MONEY,
                        RecommendationTone.BALANCED,
                        "Montar meu computador",
                        null,
                        new BigDecimal("500")
                )
        );
    }

}