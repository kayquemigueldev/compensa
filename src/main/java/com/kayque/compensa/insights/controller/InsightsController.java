package com.kayque.compensa.insights.controller;

import com.kayque.compensa.insights.model.InsightReport;
import com.kayque.compensa.insights.model.InsightsSummary;
import com.kayque.compensa.insights.repository.InsightsRepository;
import com.kayque.compensa.insights.repository.SqliteInsightsRepository;
import com.kayque.compensa.insights.service.InsightsService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InsightsController {

    private final InsightsRepository repository =
            new SqliteInsightsRepository();

    private final InsightsService service =
            new InsightsService();

    @FXML
    private Label purchaseRateLabel;

    @FXML
    private Label declineRateLabel;

    @FXML
    private Label waitingRateLabel;

    @FXML
    private Label averageTimeLabel;

    @FXML
    private Label insightHeadlineLabel;

    @FXML
    private Label insightDescriptionLabel;

    @FXML
    private Label insightsFeedbackLabel;

    @FXML
    private void initialize() {
        loadInsights();
    }

    private void loadInsights() {
        try {
            InsightsSummary summary =
                    repository.getSummary();

            InsightReport report =
                    service.generateReport(summary);

            showReport(report);

        } catch (IllegalStateException exception) {
            insightsFeedbackLabel.setText(
                    "Não foi possível carregar os insights."
            );

            insightsFeedbackLabel.getStyleClass().setAll(
                    "feedback-label",
                    "feedback-error"
            );
        }
    }

    private void showReport(InsightReport report) {
        purchaseRateLabel.setText(
                formatPercentage(report.purchaseRate())
        );

        declineRateLabel.setText(
                formatPercentage(report.declineRate())
        );

        waitingRateLabel.setText(
                formatPercentage(report.waitingRate())
        );

        averageTimeLabel.setText(
                formatWorkTime(
                        report.averageRealWorkMinutes()
                )
        );

        insightHeadlineLabel.setText(
                report.headline()
        );

        insightDescriptionLabel.setText(
                report.description()
        );
    }

    private String formatPercentage(int value) {
        return value + "%";
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