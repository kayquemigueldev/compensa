package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;

import java.util.Objects;

public class WaitingDecisionService {

    private final PurchaseDecisionRepository repository;

    public WaitingDecisionService(
            PurchaseDecisionRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório de decisões é obrigatório."
        );
    }

    public boolean markAsPurchased(long decisionId) {
        validateDecisionId(decisionId);

        return repository.finalizeWaitingDecision(
                decisionId,
                PurchaseDecisionOutcome.PURCHASED
        );
    }

    public boolean markAsDeclined(long decisionId) {
        validateDecisionId(decisionId);

        return repository.finalizeWaitingDecision(
                decisionId,
                PurchaseDecisionOutcome.DECLINED
        );
    }

    private void validateDecisionId(long decisionId) {
        if (decisionId <= 0) {
            throw new IllegalArgumentException(
                    "O ID da decisão deve ser positivo."
            );
        }
    }
}