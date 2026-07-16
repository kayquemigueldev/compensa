package com.kayque.compensa.userprofile.service;

import com.kayque.compensa.userprofile.model.PurchaseDreamImpact;
import com.kayque.compensa.userprofile.model.RecommendationTone;
import com.kayque.compensa.userprofile.model.UserGoal;
import com.kayque.compensa.userprofile.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseDreamImpactServiceTest {

    private final PurchaseDreamImpactService service =
            new PurchaseDreamImpactService();

    @Test
    void shouldCalculatePurchasePercentageOfDream() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.PLAN_A_PURCHASE,
                RecommendationTone.BALANCED,
                "Montar meu computador",
                new BigDecimal("5000.00")
        );

        PurchaseDreamImpact impact = service.calculate(
                profile,
                new BigDecimal("17.99")
        ).orElseThrow();

        assertEquals(
                "Montar meu computador",
                impact.dreamName()
        );

        assertEquals(
                new BigDecimal("0.36"),
                impact.targetPercentage()
        );
    }

    @Test
    void shouldReturnEmptyWithoutDreamTargetAmount() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.SAVE_MONEY,
                RecommendationTone.BALANCED,
                ""
        );

        Optional<PurchaseDreamImpact> impact =
                service.calculate(
                        profile,
                        new BigDecimal("17.99")
                );

        assertTrue(impact.isEmpty());
    }

    @Test
    void shouldRejectInvalidPurchaseAmount() {
        UserProfile profile = new UserProfile(
                "Kayque",
                UserGoal.PLAN_A_PURCHASE,
                RecommendationTone.BALANCED,
                "Montar meu computador",
                new BigDecimal("5000.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        profile,
                        BigDecimal.ZERO
                )
        );
    }
}