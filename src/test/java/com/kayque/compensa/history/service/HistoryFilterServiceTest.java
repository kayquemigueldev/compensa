package com.kayque.compensa.history.service;

import com.kayque.compensa.history.model.HistoryFilter;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryFilterServiceTest {

    private final HistoryFilterService service =
            new HistoryFilterService();

    @Test
    void shouldReturnAllItemsWithoutSearchOrFilter() {
        List<PurchaseDecisionHistoryItem> history =
                createHistory();

        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        history,
                        "",
                        HistoryFilter.ALL
                );

        assertEquals(3, result.size());
    }

    @Test
    void shouldSearchByProductNameIgnoringCase() {
        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        createHistory(),
                        "MONITOR",
                        HistoryFilter.ALL
                );

        assertEquals(1, result.size());
        assertEquals(
                "Monitor",
                result.getFirst().productName()
        );
    }

    @Test
    void shouldSearchIgnoringAccents() {
        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        createHistory(),
                        "cafe",
                        HistoryFilter.ALL
                );

        assertEquals(1, result.size());
        assertEquals(
                "Café",
                result.getFirst().productName()
        );
    }

    @Test
    void shouldFilterWaitingDecisions() {
        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        createHistory(),
                        "",
                        HistoryFilter.WAITING
                );

        assertEquals(1, result.size());
        assertEquals(
                PurchaseDecisionOutcome.WAITING,
                result.getFirst().outcome()
        );
    }

    @Test
    void shouldCombineSearchAndOutcomeFilter() {
        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        createHistory(),
                        "mouse",
                        HistoryFilter.DECLINED
                );

        assertEquals(1, result.size());
        assertEquals(
                "Mouse",
                result.getFirst().productName()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNothingMatches() {
        List<PurchaseDecisionHistoryItem> result =
                service.filter(
                        createHistory(),
                        "teclado",
                        HistoryFilter.PURCHASED
                );

        assertTrue(result.isEmpty());
    }

    private List<PurchaseDecisionHistoryItem> createHistory() {
        return List.of(
                createItem(
                        1,
                        "Monitor",
                        PurchaseDecisionOutcome.PURCHASED
                ),

                createItem(
                        2,
                        "Mouse",
                        PurchaseDecisionOutcome.DECLINED
                ),

                createItem(
                        3,
                        "Café",
                        PurchaseDecisionOutcome.WAITING
                )
        );
    }

    private PurchaseDecisionHistoryItem createItem(
            long id,
            String productName,
            PurchaseDecisionOutcome outcome
    ) {
        return new PurchaseDecisionHistoryItem(
                id,
                productName,
                new BigDecimal("100.00"),
                60,
                PurchaseDecisionStatus.THINK_AGAIN,
                outcome,
                null,
                Instant.parse("2026-07-19T12:00:00Z")
        );
    }
}