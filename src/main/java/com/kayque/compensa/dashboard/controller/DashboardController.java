package com.kayque.compensa.dashboard.controller;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.repository.DashboardRepository;
import com.kayque.compensa.dashboard.repository.SqliteDashboardRepository;
import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
import com.kayque.compensa.profile.service.MonthlyBudgetUsageService;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import com.kayque.compensa.purchase.repository.SqlitePurchaseDecisionRepository;
import com.kayque.compensa.purchase.service.CurrentMonthPurchasedAmountService;
import com.kayque.compensa.userprofile.repository.SqliteUserProfileRepository;
import com.kayque.compensa.userprofile.repository.UserProfileRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    private final DashboardRepository repository =
            new SqliteDashboardRepository();

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

    private final PurchaseDecisionRepository decisionRepository =
            new SqlitePurchaseDecisionRepository();

    private final MonthlyBudgetService budgetService =
            new MonthlyBudgetService();

    private final MonthlyBudgetUsageService budgetUsageService =
            new MonthlyBudgetUsageService();

    private final UserProfileRepository userProfileRepository =
            new SqliteUserProfileRepository();

    private final CurrentMonthPurchasedAmountService
            currentMonthPurchasedAmountService =
            new CurrentMonthPurchasedAmountService(
                    decisionRepository
            );

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    @FXML
    private Label plannedBudgetLabel;

    @FXML
    private Label purchasedThisMonthLabel;

    @FXML
    private Label currentAvailableBudgetLabel;

    @FXML
    private Label budgetUsagePercentageLabel;

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
    private ProgressBar budgetUsageProgressBar;

    @FXML
    private Label dashboardGreetingLabel;

    @FXML
    private void initialize() {
        loadGreeting();
        loadSummary();
        loadMonthlyBudget();
    }

    private void loadGreeting() {
        try {
            String greeting = userProfileRepository
                    .find()
                    .map(profile ->
                            "Olá, " + profile.displayName()
                    )
                    .orElse("Hoje");

            dashboardGreetingLabel.setText(greeting);

        } catch (IllegalStateException exception) {
            dashboardGreetingLabel.setText("Hoje");
        }
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

    private void loadMonthlyBudget() {
        try {
            FinancialProfile profile = profileRepository
                    .find()
                    .orElse(null);

            if (profile == null) {
                showMissingBudget();
                return;
            }

            MonthlyBudgetSummary plannedBudget =
                    budgetService.calculate(profile);

            BigDecimal purchasedThisMonth =
                    currentMonthPurchasedAmountService.calculate();

            MonthlyBudgetUsage currentBudget =
                    budgetUsageService.calculate(
                            plannedBudget,
                            purchasedThisMonth
                    );

            plannedBudgetLabel.setText(
                    currencyFormat.format(
                            currentBudget.plannedAvailableAmount()
                    )
            );

            purchasedThisMonthLabel.setText(
                    currencyFormat.format(
                            currentBudget.purchasedAmount()
                    )
            );

            currentAvailableBudgetLabel.setText(
                    currencyFormat.format(
                            currentBudget.currentAvailableAmount()
                    )
            );

            budgetUsagePercentageLabel.setText(
                    formatBudgetUsagePercentage(currentBudget)
            );

            configureBudgetProgress(currentBudget);

        } catch (IllegalStateException exception) {
            showMissingBudget();

            dashboardFeedbackLabel.setText(
                    "Não foi possível carregar o orçamento mensal."
            );

            dashboardFeedbackLabel.getStyleClass().setAll(
                    "feedback-label",
                    "feedback-error"
            );
        }
    }

    private String formatBudgetUsagePercentage(
            MonthlyBudgetUsage budget
    ) {
        BigDecimal plannedAmount =
                budget.plannedAvailableAmount();

        if (plannedAmount.signum() <= 0) {
            return "Sem orçamento livre planejado";
        }

        BigDecimal percentage =
                budget.purchasedAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(
                                plannedAmount,
                                1,
                                RoundingMode.HALF_UP
                        );

        return String.format(
                Locale.of("pt", "BR"),
                "%.1f%% utilizado",
                percentage
        );
    }

    private void configureBudgetProgress(
            MonthlyBudgetUsage budget
    ) {
        BigDecimal plannedAmount =
                budget.plannedAvailableAmount();

        if (plannedAmount.signum() <= 0) {
            budgetUsageProgressBar.setProgress(0);

            budgetUsageProgressBar
                    .getStyleClass()
                    .setAll(
                            "budget-progress-bar",
                            "budget-progress-warning"
                    );

            return;
        }

        BigDecimal usageRatio =
                budget.purchasedAmount()
                        .divide(
                                plannedAmount,
                                4,
                                RoundingMode.HALF_UP
                        );

        double progress = Math.max(
                0,
                Math.min(usageRatio.doubleValue(), 1)
        );

        budgetUsageProgressBar.setProgress(progress);

        String statusStyle;

        if (usageRatio.compareTo(new BigDecimal("0.90")) >= 0) {
            statusStyle = "budget-progress-negative";
        } else if (
                usageRatio.compareTo(new BigDecimal("0.70")) >= 0
        ) {
            statusStyle = "budget-progress-warning";
        } else {
            statusStyle = "budget-progress-positive";
        }

        budgetUsageProgressBar
                .getStyleClass()
                .setAll(
                        "budget-progress-bar",
                        statusStyle
                );
    }

    private void showMissingBudget() {
        plannedBudgetLabel.setText("--");
        purchasedThisMonthLabel.setText("--");
        currentAvailableBudgetLabel.setText("--");
        budgetUsagePercentageLabel.setText(
                "Configure seu perfil financeiro"
        );

        budgetUsageProgressBar.setProgress(0);

        budgetUsageProgressBar
                .getStyleClass()
                .setAll(
                        "budget-progress-bar",
                        "budget-progress-warning"
                );

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