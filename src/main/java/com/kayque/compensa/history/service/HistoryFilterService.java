package com.kayque.compensa.history.service;

import com.kayque.compensa.history.model.HistoryFilter;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HistoryFilterService {

    public List<PurchaseDecisionHistoryItem> filter(
            List<PurchaseDecisionHistoryItem> history,
            String searchText,
            HistoryFilter filter
    ) {
        Objects.requireNonNull(
                history,
                "O histórico é obrigatório."
        );

        HistoryFilter selectedFilter =
                filter == null
                        ? HistoryFilter.ALL
                        : filter;

        String normalizedSearch =
                normalize(searchText);

        return history.stream()
                .filter(item ->
                        matchesSearch(
                                item,
                                normalizedSearch
                        )
                )
                .filter(item ->
                        matchesFilter(
                                item,
                                selectedFilter
                        )
                )
                .toList();
    }

    private boolean matchesSearch(
            PurchaseDecisionHistoryItem item,
            String normalizedSearch
    ) {
        if (normalizedSearch.isBlank()) {
            return true;
        }

        return normalize(item.productName())
                .contains(normalizedSearch);
    }

    private boolean matchesFilter(
            PurchaseDecisionHistoryItem item,
            HistoryFilter filter
    ) {
        return switch (filter) {
            case ALL -> true;

            case PURCHASED ->
                    item.outcome()
                            == PurchaseDecisionOutcome.PURCHASED;

            case DECLINED ->
                    item.outcome()
                            == PurchaseDecisionOutcome.DECLINED;

            case WAITING ->
                    item.outcome()
                            == PurchaseDecisionOutcome.WAITING;
        };
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return withoutAccents
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}