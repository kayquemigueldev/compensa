package com.kayque.compensa.purchase.repository;

import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;
import java.math.BigDecimal;
import java.time.Instant;

import java.util.List;

public interface PurchaseDecisionRepository {

    long save(PurchaseDecision decision);

    List<PurchaseDecisionHistoryItem> findAll();

    List<PurchaseDecisionHistoryItem> findWaiting();

    boolean finalizeWaitingDecision(
            long decisionId,
            PurchaseDecisionOutcome finalOutcome
    );

    boolean evaluatePurchasedDecision(
            long decisionId,
            PurchaseSatisfaction satisfaction
    );

    BigDecimal sumPurchasedAmountBetween(
            Instant startInclusive,
            Instant endExclusive
    );

}