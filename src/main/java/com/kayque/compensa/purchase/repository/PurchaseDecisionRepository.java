package com.kayque.compensa.purchase.repository;

import com.kayque.compensa.purchase.model.PurchaseDecision;

public interface PurchaseDecisionRepository {

    long save(PurchaseDecision decision);
}