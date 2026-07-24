package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.util.Objects;
import java.util.Optional;

public class PurchaseEvaluationAlertRule
        implements SmartAlertRule {

    private static final int CRITICAL_AMOUNT = 3;

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        if (!snapshot.hasUnevaluatedPurchases()) {
            return Optional.empty();
        }

        int amount = snapshot.unevaluatedPurchases();

        if (amount >= CRITICAL_AMOUNT) {
            return Optional.of(
                    new SmartAlert(
                            "purchase-evaluation.accumulated",
                            SmartAlertTopic.PURCHASE_EVALUATION,
                            SmartAlertPriority.CRITICAL,
                            amount + " compras ainda não foram avaliadas",
                            "Registre se elas valeram a pena. "
                                    + "Essas avaliações ajudam o Compensa? "
                                    + "a reconhecer seus padrões de consumo."
                    )
            );
        }

        return Optional.of(
                new SmartAlert(
                        "purchase-evaluation.pending",
                        SmartAlertTopic.PURCHASE_EVALUATION,
                        SmartAlertPriority.ATTENTION,
                        createTitle(amount),
                        createMessage(amount)
                )
        );
    }

    private String createTitle(int amount) {
        if (amount == 1) {
            return "Uma compra ainda não foi avaliada";
        }

        return amount + " compras ainda não foram avaliadas";
    }

    private String createMessage(int amount) {
        if (amount == 1) {
            return "Conte se essa compra valeu a pena para tornar "
                    + "seus próximos insights mais precisos.";
        }

        return "Conte se essas compras valeram a pena para tornar "
                + "seus próximos insights mais precisos.";
    }
}