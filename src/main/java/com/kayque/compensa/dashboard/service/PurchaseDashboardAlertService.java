package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.dashboard.model.PurchaseAttentionSummary;

import java.util.Objects;
import java.util.Optional;

public class PurchaseDashboardAlertService {

    public Optional<DashboardAlert> create(
            PurchaseAttentionSummary summary
    ) {
        Objects.requireNonNull(
                summary,
                "O resumo das pendências é obrigatório."
        );

        if (!summary.hasPendingItems()) {
            return Optional.empty();
        }

        if (summary.waitingDecisions() > 0
                && summary.unevaluatedPurchases() > 0) {
            return Optional.of(
                    new DashboardAlert(
                            "Existem decisões para revisar",
                            createCombinedMessage(summary),
                            DashboardAlertLevel.ATTENTION
                    )
            );
        }

        if (summary.waitingDecisions() > 0) {
            return Optional.of(
                    new DashboardAlert(
                            waitingTitle(
                                    summary.waitingDecisions()
                            ),
                            waitingMessage(
                                    summary.waitingDecisions()
                            ),
                            DashboardAlertLevel.INFORMATION
                    )
            );
        }

        return Optional.of(
                new DashboardAlert(
                        evaluationTitle(
                                summary.unevaluatedPurchases()
                        ),
                        evaluationMessage(
                                summary.unevaluatedPurchases()
                        ),
                        DashboardAlertLevel.ATTENTION
                )
        );
    }

    private String createCombinedMessage(
            PurchaseAttentionSummary summary
    ) {
        return waitingMessage(
                summary.waitingDecisions()
        ) + " " + evaluationMessage(
                summary.unevaluatedPurchases()
        );
    }

    private String waitingTitle(long amount) {
        return amount == 1
                ? "Uma decisão está esperando por você"
                : amount
                  + " decisões estão esperando por você";
    }

    private String waitingMessage(long amount) {
        return amount == 1
                ? "Você possui uma compra aguardando uma decisão."
                : "Você possui "
                  + amount
                  + " compras aguardando uma decisão.";
    }

    private String evaluationTitle(long amount) {
        return amount == 1
                ? "Como foi sua compra?"
                : "Como foram suas compras?";
    }

    private String evaluationMessage(long amount) {
        return amount == 1
                ? "Uma compra realizada ainda não foi avaliada."
                : amount
                  + " compras realizadas ainda não foram avaliadas.";
    }
}