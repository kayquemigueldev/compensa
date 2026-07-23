package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class PreservedValueAlertRule
        implements SmartAlertRule {

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        BigDecimal preservedAmount =
                snapshot.preservedAmountThisYear();

        if (preservedAmount.signum() <= 0) {
            return Optional.empty();
        }

        return Optional.of(
                new SmartAlert(
                        "preserved-value.year-total",
                        SmartAlertTopic.PRESERVED_VALUE,
                        SmartAlertPriority.INFORMATIONAL,
                        "Suas decisões já preservaram dinheiro",
                        "Você evitou "
                                + formatMoney(preservedAmount)
                                + " em compras neste ano."
                )
        );
    }

    private String formatMoney(BigDecimal amount) {
        return currencyFormat
                .format(amount)
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ');
    }
}