package com.kayque.compensa.purchase.repository;

import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;

import java.util.List;

public interface PurchaseDecisionRepository {

    long save(PurchaseDecision decision);

    List<PurchaseDecisionHistoryItem> findAll();
}