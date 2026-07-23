package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.util.Objects;
import java.util.Optional;

public class PurchaseBehaviorAlertRule
        implements SmartAlertRule {

    private static final int HIGH_PURCHASE_COUNT = 5;
    private static final int PURCHASE_DOMINANCE_MULTIPLIER = 2;

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        int purchasesMade =
                snapshot.purchasesMade();

        int purchasesAvoided =
                snapshot.purchasesAvoided();

        if (purchasesMade == 0
                && purchasesAvoided == 0) {
            return Optional.empty();
        }

        if (purchasesMade >= HIGH_PURCHASE_COUNT
                && purchasesMade
                > purchasesAvoided
                * PURCHASE_DOMINANCE_MULTIPLIER) {
            return Optional.of(
                    new SmartAlert(
                            "purchase-behavior.high-purchases",
                            SmartAlertTopic.PURCHASE_BEHAVIOR,
                            SmartAlertPriority.ATTENTION,
                            "Compras realizadas estão predominando",
                            "Você realizou "
                                    + formatPurchases(
                                    purchasesMade
                            )
                                    + " e evitou "
                                    + formatPurchases(
                                    purchasesAvoided
                            )
                                    + ". Vale revisar as próximas decisões com mais calma."
                    )
            );
        }

        if (purchasesAvoided > purchasesMade) {
            return Optional.of(
                    new SmartAlert(
                            "purchase-behavior.positive-balance",
                            SmartAlertTopic.PURCHASE_BEHAVIOR,
                            SmartAlertPriority.INFORMATIONAL,
                            "Suas escolhas estão protegendo seu orçamento",
                            "Você evitou "
                                    + formatPurchases(
                                    purchasesAvoided
                            )
                                    + " e realizou "
                                    + formatPurchases(
                                    purchasesMade
                            )
                                    + "."
                    )
            );
        }

        if (purchasesAvoided > 0) {
            return Optional.of(
                    new SmartAlert(
                            "purchase-behavior.avoided-purchases",
                            SmartAlertTopic.PURCHASE_BEHAVIOR,
                            SmartAlertPriority.INFORMATIONAL,
                            "Você está tomando decisões conscientes",
                            "Você evitou "
                                    + formatPurchases(
                                    purchasesAvoided
                            )
                                    + " neste período."
                    )
            );
        }

        return Optional.empty();
    }

    private String formatPurchases(int amount) {
        if (amount == 1) {
            return "1 compra";
        }

        return amount + " compras";
    }
}