package com.kayque.compensa.purchase.repository;

import java.math.BigDecimal;
import java.time.Instant;

@FunctionalInterface
public interface PurchasedAmountRepository {

    BigDecimal sumPurchasedAmountBetween(
            Instant startInclusive,
            Instant endExclusive
    );
}