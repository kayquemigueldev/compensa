package com.kayque.compensa.dashboard.controller;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.dashboard.repository.SqliteDashboardRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    private final DashboardRepository repository =
            new SqliteDashboardRepository();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private Label totalDecisionsLabel;

    @FXML
    private Label purchasedDecisionsLabel;

    @FXML
    private Label declinedDecisionsLabel;

    @FXML
    private Label waitingDecisionsLabel;

    @FXML
    private Label preservedValueLabel;

    @FXML
    private Label totalWorkTimeLabel;

    @FXML
    private Label dashboardFeedbackLabel;

    @FXML
    private void initialize() {
        loadSummary();
    }

    private void loadSummary() {
        try {
            DashboardSummary summary =
                    repository.getSummary();

            totalDecisionsLabel.setText(
                    String.valueOf(summary.totalDecisions())
            );

            purchasedDecisionsLabel.setText(
                    String.valueOf(
                            summary.purchasedDecisions()
                    )
            );

            declinedDecisionsLabel.setText(
                    String.valueOf(
                            summary.declinedDecisions()
                    )
            );

            waitingDecisionsLabel.setText(
                    String.valueOf(
                            summary.waitingDecisions()
                    )
            );

            preservedValueLabel.setText(
                    currencyFormat.format(
                            summary.preservedValue()
                    )
            );

            totalWorkTimeLabel.setText(
                    formatWorkTime(
                            summary.totalRealWorkMinutes()
                    )
            );

        } catch (IllegalStateException exception) {
            dashboardFeedbackLabel.setText(
                    "Não foi possível carregar os indicadores."
            );

            dashboardFeedbackLabel.getStyleClass().setAll(
                    "feedback-label",
                    "feedback-error"
            );
        }
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
}