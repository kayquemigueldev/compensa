package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitingDecisionServiceTest {

    @Test
    void shouldFinalizeWaitingDecisionAsPurchased() {
        FakeRepository repository = new FakeRepository();

        WaitingDecisionService service =
                new WaitingDecisionService(repository);

        boolean updated = service.markAsPurchased(12);

        assertTrue(updated);
        assertEquals(12, repository.decisionId);
        assertEquals(
                PurchaseDecisionOutcome.PURCHASED,
                repository.outcome
        );
    }

    @Test
    void shouldFinalizeWaitingDecisionAsDeclined() {
        FakeRepository repository = new FakeRepository();

        WaitingDecisionService service =
                new WaitingDecisionService(repository);

        boolean updated = service.markAsDeclined(18);

        assertTrue(updated);
        assertEquals(18, repository.decisionId);
        assertEquals(
                PurchaseDecisionOutcome.DECLINED,
                repository.outcome
        );
    }

    @Test
    void shouldRejectInvalidDecisionId() {
        WaitingDecisionService service =
                new WaitingDecisionService(
                        new FakeRepository()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.markAsPurchased(0)
        );
    }

    private static final class FakeRepository
            implements PurchaseDecisionRepository {

        private long decisionId;
        private PurchaseDecisionOutcome outcome;

        @Override
        public boolean finalizeWaitingDecision(
                long decisionId,
                PurchaseDecisionOutcome finalOutcome
        ) {
            this.decisionId = decisionId;
            this.outcome = finalOutcome;
            return true;
        }

        @Override
        public long save(PurchaseDecision decision) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PurchaseDecisionHistoryItem> findAll() {
            return List.of();
        }

        @Override
        public List<PurchaseDecisionHistoryItem> findWaiting() {
            return List.of();
        }

        @Override
        public boolean evaluatePurchasedDecision(
                long decisionId,
                PurchaseSatisfaction satisfaction
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal sumPurchasedAmountBetween(
                Instant startInclusive,
                Instant endExclusive
        ) {
            return BigDecimal.ZERO;
        }
        @Override
        public boolean deleteById(long decisionId) {
            return false;
        }
    }
}