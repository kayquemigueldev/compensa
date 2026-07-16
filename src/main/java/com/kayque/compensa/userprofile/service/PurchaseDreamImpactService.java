package com.kayque.compensa.userprofile.service;

import com.kayque.compensa.userprofile.model.PurchaseDreamImpact;
import com.kayque.compensa.userprofile.model.UserProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class PurchaseDreamImpactService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    public Optional<PurchaseDreamImpact> calculate(
            UserProfile profile,
            BigDecimal purchaseAmount
    ) {
        Objects.requireNonNull(
                profile,
                "O perfil do usuário é obrigatório."
        );

        if (purchaseAmount == null
                || purchaseAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "O valor da compra deve ser positivo."
            );
        }

        if (!profile.hasCurrentDream()
                || !profile.hasCurrentDreamTargetAmount()) {
            return Optional.empty();
        }

        BigDecimal percentage = purchaseAmount
                .multiply(ONE_HUNDRED)
                .divide(
                        profile.currentDreamTargetAmount(),
                        2,
                        RoundingMode.HALF_UP
                );

        return Optional.of(
                new PurchaseDreamImpact(
                        profile.currentDream(),
                        profile.currentDreamTargetAmount(),
                        purchaseAmount,
                        percentage
                )
        );
    }
}