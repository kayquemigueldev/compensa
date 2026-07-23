package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.util.Objects;
import java.util.Optional;

public class PendingDecisionAlertRule
        implements SmartAlertRule {

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        if (snapshot.hasOverduePendingDecisions()) {
            int overdueDecisions =
                    snapshot.overduePendingDecisions();

            return Optional.of(
                    new SmartAlert(
                            "pending-decisions.overdue",
                            SmartAlertTopic.PENDING_DECISIONS,
                            SmartAlertPriority.CRITICAL,
                            createOverdueTitle(overdueDecisions),
                            createOverdueMessage(overdueDecisions)
                    )
            );
        }

        if (snapshot.hasPendingDecisions()) {
            int pendingDecisions =
                    snapshot.pendingDecisions();

            return Optional.of(
                    new SmartAlert(
                            "pending-decisions.waiting",
                            SmartAlertTopic.PENDING_DECISIONS,
                            SmartAlertPriority.ATTENTION,
                            createPendingTitle(pendingDecisions),
                            createPendingMessage(pendingDecisions)
                    )
            );
        }

        return Optional.empty();
    }

    private String createOverdueTitle(int amount) {
        if (amount == 1) {
            return "Uma decisão está aguardando há mais de 7 dias";
        }

        return amount
                + " decisões estão aguardando há mais de 7 dias";
    }

    private String createOverdueMessage(int amount) {
        if (amount == 1) {
            return "Reavalie essa decisão antes que ela deixe "
                    + "de representar sua situação atual.";
        }

        return "Reavalie essas decisões antes que elas deixem "
                + "de representar sua situação atual.";
    }

    private String createPendingTitle(int amount) {
        if (amount == 1) {
            return "Você possui uma decisão aguardando resposta";
        }

        return "Você possui "
                + amount
                + " decisões aguardando resposta";
    }

    private String createPendingMessage(int amount) {
        if (amount == 1) {
            return "Quando estiver pronto, volte à decisão "
                    + "e registre o que escolheu.";
        }

        return "Quando estiver pronto, volte às decisões "
                + "e registre o que escolheu.";
    }
}