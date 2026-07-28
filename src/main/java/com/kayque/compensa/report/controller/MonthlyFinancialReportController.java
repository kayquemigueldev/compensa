package com.kayque.compensa.report.controller;

import com.kayque.compensa.dashboard.repository.SqliteDashboardRepository;
import com.kayque.compensa.report.model.MonthlyFinancialOverview;
import com.kayque.compensa.report.model.MonthlyFinancialReport;
import com.kayque.compensa.report.model.MonthlySpendingTrend;
import com.kayque.compensa.report.service.MonthlyFinancialReportService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MonthlyFinancialReportController {

    private static final Locale BRAZILIAN_LOCALE =
            Locale.of("pt", "BR");

    private final MonthlyFinancialReportService reportService =
            new MonthlyFinancialReportService(
                    new SqliteDashboardRepository()
            );

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    BRAZILIAN_LOCALE
            );

    private final DateTimeFormatter monthFormat =
            DateTimeFormatter.ofPattern(
                    "MMMM 'de' yyyy",
                    BRAZILIAN_LOCALE
            );

    private YearMonth selectedMonth;

    @FXML
    private Label selectedMonthLabel;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Label totalDecisionsLabel;

    @FXML
    private Label purchasedDecisionsLabel;

    @FXML
    private Label declinedDecisionsLabel;

    @FXML
    private Label waitingDecisionsLabel;

    @FXML
    private Label purchasedValueLabel;

    @FXML
    private Label preservedValueLabel;

    @FXML
    private Label workTimeLabel;

    @FXML
    private Label previousMonthLabel;

    @FXML
    private Label previousPurchasedValueLabel;

    @FXML
    private Label spendingTrendTitleLabel;

    @FXML
    private Label spendingTrendDescriptionLabel;

    @FXML
    private Label variationPercentageLabel;

    @FXML
    private Label reportFeedbackLabel;

    @FXML
    private void initialize() {
        selectedMonth = YearMonth.now();

        loadReport();
    }

    @FXML
    private void showPreviousMonth() {
        selectedMonth =
                selectedMonth.minusMonths(1);

        loadReport();
    }

    @FXML
    private void showNextMonth() {
        YearMonth nextMonth =
                selectedMonth.plusMonths(1);

        if (nextMonth.isAfter(YearMonth.now())) {
            return;
        }

        selectedMonth = nextMonth;

        loadReport();
    }

    private void loadReport() {
        clearFeedback();

        try {
            MonthlyFinancialOverview overview =
                    reportService.createComparison(
                            selectedMonth
                    );

            renderOverview(overview);

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível carregar o relatório mensal."
            );
        }
    }

    private void renderOverview(
            MonthlyFinancialOverview overview
    ) {
        MonthlyFinancialReport current =
                overview.currentMonth();

        MonthlyFinancialReport previous =
                overview.previousMonth();

        selectedMonthLabel.setText(
                formatMonth(overview.selectedMonth())
        );

        totalDecisionsLabel.setText(
                String.valueOf(
                        current.totalDecisions()
                )
        );

        purchasedDecisionsLabel.setText(
                String.valueOf(
                        current.purchasedDecisions()
                )
        );

        declinedDecisionsLabel.setText(
                String.valueOf(
                        current.declinedDecisions()
                )
        );

        waitingDecisionsLabel.setText(
                String.valueOf(
                        current.waitingDecisions()
                )
        );

        purchasedValueLabel.setText(
                currencyFormat.format(
                        current.purchasedValue()
                )
        );

        preservedValueLabel.setText(
                currencyFormat.format(
                        current.preservedValue()
                )
        );

        workTimeLabel.setText(
                formatWorkTime(
                        current.totalRealWorkMinutes()
                )
        );

        previousMonthLabel.setText(
                formatMonth(previous.month())
        );

        previousPurchasedValueLabel.setText(
                currencyFormat.format(
                        previous.purchasedValue()
                )
        );

        variationPercentageLabel.setText(
                formatVariation(
                        overview.spendingVariationPercentage()
                )
        );

        renderTrend(
                overview.spendingTrend()
        );

        boolean currentMonth =
                selectedMonth.equals(
                        YearMonth.now()
                );

        nextMonthButton.setDisable(currentMonth);
    }

    private void renderTrend(
            MonthlySpendingTrend trend
    ) {
        TrendMessage message =
                createTrendMessage(trend);

        spendingTrendTitleLabel.setText(
                message.title()
        );

        spendingTrendDescriptionLabel.setText(
                message.description()
        );

        spendingTrendTitleLabel
                .getStyleClass()
                .setAll(
                        "report-trend-title",
                        message.styleClass()
                );

        variationPercentageLabel
                .getStyleClass()
                .setAll(
                        "report-variation-value",
                        message.styleClass()
                );
    }

    private TrendMessage createTrendMessage(
            MonthlySpendingTrend trend
    ) {
        return switch (trend) {
            case LOWER -> new TrendMessage(
                    "Seus gastos diminuíram",
                    "Você gastou menos em compras do que no mês anterior.",
                    "report-trend-positive"
            );

            case HIGHER -> new TrendMessage(
                    "Seus gastos aumentaram",
                    "Você gastou mais em compras do que no mês anterior.",
                    "report-trend-warning"
            );

            case STABLE -> new TrendMessage(
                    "Seus gastos permaneceram estáveis",
                    "O total comprado foi igual ao registrado no mês anterior.",
                    "report-trend-neutral"
            );

            case FIRST_PURCHASES -> new TrendMessage(
                    "Primeiras compras do período",
                    "Não havia compras no mês anterior para realizar uma comparação.",
                    "report-trend-neutral"
            );

            case NO_PURCHASES -> new TrendMessage(
                    "Nenhuma compra registrada",
                    "Não existem compras neste mês nem no mês anterior.",
                    "report-trend-positive"
            );
        };
    }

    private String formatMonth(YearMonth month) {
        String formatted =
                month.format(monthFormat);

        return Character.toUpperCase(
                formatted.charAt(0)
        ) + formatted.substring(1);
    }

    private String formatVariation(
            BigDecimal variation
    ) {
        if (variation.signum() == 0) {
            return "0%";
        }

        return variation
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }

    private String formatWorkTime(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + " min";
        }

        if (minutes == 0) {
            return hours + "h";
        }

        return hours + "h " + minutes + "min";
    }

    private void clearFeedback() {
        reportFeedbackLabel.setText("");

        reportFeedbackLabel
                .getStyleClass()
                .setAll("feedback-label");
    }

    private void showError(String message) {
        reportFeedbackLabel.setText(message);

        reportFeedbackLabel
                .getStyleClass()
                .setAll(
                        "feedback-label",
                        "feedback-error"
                );
    }

    private record TrendMessage(
            String title,
            String description,
            String styleClass
    ) {
    }
}