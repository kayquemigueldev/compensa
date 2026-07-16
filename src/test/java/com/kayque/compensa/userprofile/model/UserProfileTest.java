package com.kayque.compensa.userprofile.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}