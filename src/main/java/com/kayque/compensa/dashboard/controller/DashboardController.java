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
import com.kayque.compensa.dashboard.model.DashboardHighlight;
import com.kayque.compensa.dashboard.service.DashboardHighlightService;
import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPace;
import com.kayque.compensa.goal.repository.SavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalContributionRepository;
import com.kayque.compensa.goal.service.SavingsGoalForecastService;
import com.kayque.compensa.goal.model.SavingsGoalConsistency;
import com.kayque.compensa.goal.model.SavingsGoalConsistencyStatus;
import com.kayque.compensa.goal.service.SavingsGoalConsistencyService;
import com.kayque.compensa.alerts.service.SmartAlertService;
import com.kayque.compensa.alerts.service.SmartAlertSnoozeService;
import com.kayque.compensa.alerts.service.SmartAlertServiceFactory;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertSnoozeRepository;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import com.kayque.compensa.dashboard.service.DashboardSmartAlertPresentationService;
import com.kayque.compensa.navigation.NavigationRequestEvent;
import com.kayque.compensa.navigation.NavigationTarget;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.AccessibleRole;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.Clock;

import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalRepository;
import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import com.kayque.compensa.goal.service.SavingsGoalTargetPlanService;
import com.kayque.compensa.goal.service.SavingsGoalMonthlyPaceService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class DashboardController {

    private final DashboardRepository repository =
            new SqliteDashboardRepository();

    private final FinancialProfileRepository profileRepository =
            new SqliteFinancialProfileRepository();

    private final PurchaseDecisionRepository decisionRepository =
            new SqlitePurchaseDecisionRepository();

    private final SavingsGoalRepository savingsGoalRepository =
            new SqliteSavingsGoalRepository();

    private final SavingsGoalProgressService
            savingsGoalProgressService =
            new SavingsGoalProgressService();

    private final DashboardHighlightService
            dashboardHighlightService =
            new DashboardHighlightService();

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

    private final SavingsGoalContributionRepository
            savingsGoalContributionRepository =
            new SqliteSavingsGoalContributionRepository();

    private final SavingsGoalForecastService
            savingsGoalForecastService =
            new SavingsGoalForecastService();

    private final SavingsGoalTargetPlanService
            savingsGoalTargetPlanService =
            new SavingsGoalTargetPlanService();

    private final SavingsGoalMonthlyPaceService
            savingsGoalMonthlyPaceService =
            new SavingsGoalMonthlyPaceService();

    private final SavingsGoalConsistencyService
            savingsGoalConsistencyService =
            new SavingsGoalConsistencyService();

    private static final int SMART_ALERT_LIMIT = 3;

    private final SmartAlertService smartAlertService =
            SmartAlertServiceFactory.createDefault();

    private final SmartAlertSnoozeService
            smartAlertSnoozeService =
            new SmartAlertSnoozeService(
                    new SqliteSmartAlertSnoozeRepository(),
                    Clock.systemDefaultZone()
            );

    private final DashboardSmartAlertPresentationService
            smartAlertPresentationService =
            new DashboardSmartAlertPresentationService();

    private final DateTimeFormatter goalForecastDateFormat =
            DateTimeFormatter.ofPattern(
                    "MMMM 'de' yyyy",
                    Locale.of("pt", "BR")
            );


    private DashboardSummary currentSummary;
    private SavingsGoal currentSavingsGoal;
    private SavingsGoalProgress currentGoalProgress;

    @FXML
    private Label dashboardGoalForecastDateLabel;

    @FXML
    private Label dashboardGoalForecastLabel;

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
    private VBox dashboardGoalCard;

    @FXML
    private Label dashboardGoalNameLabel;

    @FXML
    private Label dashboardGoalPercentageLabel;

    @FXML
    private Label dashboardGoalSavedLabel;

    @FXML
    private Label dashboardGoalRemainingLabel;

    @FXML
    private ProgressBar dashboardGoalProgressBar;

    @FXML
    private VBox dashboardHighlightCard;

    @FXML
    private Label dashboardHighlightTitleLabel;

    @FXML
    private Label dashboardHighlightDescriptionLabel;

    @FXML
    private Label dashboardBudgetAlertTitleLabel;

    @FXML
    private Label dashboardBudgetAlertMessageLabel;

    @FXML
    private Label dashboardGoalLastContributionLabel;

    @FXML
    private Label dashboardGoalTargetPlanLabel;

    @FXML
    private Label dashboardGoalTargetPlanMessageLabel;

    @FXML
    private Region dashboardGoalTargetPlanSeparator;

    @FXML
    private Label dashboardGoalMonthlyPaceLabel;

    @FXML
    private Label dashboardGoalMonthlyPaceMessageLabel;

    @FXML
    private Region dashboardGoalConsistencySeparator;

    @FXML
    private Label dashboardGoalConsistencyLabel;

    @FXML
    private Label dashboardGoalConsistencyMessageLabel;

    @FXML
    private VBox dashboardSmartAlertsSection;

    @FXML
    private HBox dashboardSmartAlertsContainer;

    @FXML
    private void initialize() {
        configureDashboardShortcuts();

        loadGreeting();
        loadSummary();
        loadMonthlyBudget();
        loadSavingsGoal();
        showDashboardHighlight();
        loadSmartAlerts();
    }

    private void configureDashboardShortcuts() {
        configureNavigationShortcut(
                dashboardGoalCard,
                NavigationTarget.SAVINGS_GOAL,
                "Abrir detalhes do objetivo financeiro"
        );
    }

    private void configureNavigationShortcut(
            Node node,
            NavigationTarget target,
            String accessibleText
    ) {
        node.getStyleClass().add(
                "dashboard-navigation-shortcut"
        );

        node.setFocusTraversable(true);
        node.setAccessibleRole(
                AccessibleRole.BUTTON
        );

        node.setAccessibleText(accessibleText);

        node.setOnMouseClicked(event ->
                requestNavigation(target)
        );

        node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    || event.getCode() == KeyCode.SPACE) {
                requestNavigation(target);
                event.consume();
            }
        });
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
            currentSummary = summary;

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
            currentSummary = null;
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

    private void loadSavingsGoal() {
        try {
            savingsGoalRepository.find().ifPresentOrElse(
                    goal -> {
                        SavingsGoalProgress progress =
                                savingsGoalProgressService.calculate(
                                        goal
                                );

                        currentSavingsGoal = goal;
                        currentGoalProgress = progress;

                        dashboardGoalNameLabel.setText(
                                goal.name()
                        );

                        dashboardGoalPercentageLabel.setText(
                                formatGoalPercentage(
                                        progress.percentage()
                                )
                        );

                        dashboardGoalSavedLabel.setText(
                                currencyFormat.format(
                                        progress.savedAmount()
                                )
                        );

                        dashboardGoalRemainingLabel.setText(
                                currencyFormat.format(
                                        progress.remainingAmount()

                                )
                        );

                        double progressValue =
                                progress.percentage()
                                        .divide(
                                                new BigDecimal("100"),
                                                4,
                                                RoundingMode.HALF_UP
                                        )
                                        .doubleValue();

                        dashboardGoalProgressBar.setProgress(
                                Math.max(
                                        0,
                                        Math.min(progressValue, 1)
                                )
                        );

                        renderGoalTargetPlan(goal);
                        renderGoalMonthlyPace(goal);
                        renderGoalForecast(goal);
                        renderLastGoalContribution();
                        renderGoalConsistency();

                        dashboardGoalCard.setAccessibleText(
                                "Abrir objetivo "
                                        + goal.name()
                                        + ". "
                                        + formatGoalShortcutProgress(progress)
                        );

                        dashboardGoalCard.setVisible(true);
                        dashboardGoalCard.setManaged(true);
                    },
                    () -> {
                        currentSavingsGoal = null;
                        currentGoalProgress = null;
                        hideSavingsGoal();
                    }
            );

        } catch (IllegalStateException exception) {
            currentSavingsGoal = null;
            currentGoalProgress = null;
            hideSavingsGoal();
        }
    }

    private String formatGoalPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }

    private void showDashboardHighlight() {
        if (currentSummary == null) {
            showDefaultHighlight();
            return;
        }

        String goalName = currentSavingsGoal == null
                ? null
                : currentSavingsGoal.name();

        DashboardHighlight highlight =
                dashboardHighlightService.create(
                        currentSummary,
                        goalName,
                        currentGoalProgress
                );

        dashboardHighlightTitleLabel.setText(
                highlight.title()
        );

        dashboardHighlightDescriptionLabel.setText(
                highlight.description()
        );

        dashboardHighlightCard.getStyleClass().setAll(
                "dashboard-insight-card",
                getHighlightStyleClass(highlight)
        );
    }

    private String getHighlightStyleClass(
            DashboardHighlight highlight
    ) {
        return switch (highlight.type()) {
            case SUCCESS ->
                    "dashboard-highlight-success";

            case GOAL ->
                    "dashboard-highlight-goal";

            case WARNING ->
                    "dashboard-highlight-warning";

            case PRESERVED_VALUE ->
                    "dashboard-highlight-preserved";

            case DEFAULT ->
                    "dashboard-highlight-default";
        };
    }

    private void showDefaultHighlight() {
        dashboardHighlightTitleLabel.setText(
                "Uma escolha de cada vez"
        );

        dashboardHighlightDescriptionLabel.setText(
                "O objetivo não é parar de comprar. É entender quando uma compra realmente faz sentido para você."
        );

        dashboardHighlightCard.getStyleClass().setAll(
                "dashboard-insight-card",
                "dashboard-highlight-default"
        );
    }

    private void hideSavingsGoal() {
        hideGoalTargetPlan();
        hideGoalMonthlyPace();

        dashboardGoalForecastLabel.setText("");

        hideGoalForecastDate();
        hideLastGoalContribution();

        dashboardGoalCard.setVisible(false);
        dashboardGoalCard.setManaged(false);
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

    private void renderGoalConsistency() {
        try {
            List<SavingsGoalContribution> contributions =
                    savingsGoalContributionRepository.findAll();

            SavingsGoalConsistency consistency =
                    savingsGoalConsistencyService.calculate(
                            contributions,
                            LocalDate.now()
                    );

            dashboardGoalConsistencyLabel.setText(
                    getGoalConsistencyTitle(consistency)
            );

            dashboardGoalConsistencyMessageLabel.setText(
                    getGoalConsistencyMessage(consistency)
            );

            dashboardGoalConsistencyMessageLabel
                    .getStyleClass()
                    .setAll(
                            "dashboard-goal-consistency-message",
                            getGoalConsistencyStyle(
                                    consistency.status()
                            )
                    );

            setGoalConsistencyVisible(true);

        } catch (IllegalStateException exception) {
            setGoalConsistencyVisible(false);
        }
    }

    private String getGoalConsistencyTitle(
            SavingsGoalConsistency consistency
    ) {
        return switch (consistency.status()) {
            case NO_CONTRIBUTIONS ->
                    "Ritmo da conquista";

            case STARTED_THIS_MONTH ->
                    currencyFormat.format(
                            consistency.contributedThisMonth()
                    ) + " guardados neste mês";

            case CONSISTENT ->
                    consistency.consecutiveMonths()
                            + " "
                            + formatMonthWord(
                            consistency.consecutiveMonths()
                    )
                            + " de consistência";

            case PAUSED ->
                    "Seu objetivo espera por você";
        };
    }

    private String getGoalConsistencyMessage(
            SavingsGoalConsistency consistency
    ) {
        return switch (consistency.status()) {
            case NO_CONTRIBUTIONS ->
                    "Registre sua primeira contribuição para começar a construir seu ritmo.";

            case STARTED_THIS_MONTH ->
                    consistency.contributionsThisMonth()
                            + " "
                            + formatContributionWord(
                            consistency.contributionsThisMonth()
                    )
                            + " neste mês. Continue avançando.";

            case CONSISTENT ->
                    "Você contribuiu por "
                            + consistency.consecutiveMonths()
                            + " "
                            + formatMonthWord(
                            consistency.consecutiveMonths()
                    )
                            + " seguidos e guardou "
                            + currencyFormat.format(
                            consistency.contributedThisMonth()
                    )
                            + " neste mês.";

            case PAUSED ->
                    "Ainda não houve contribuição neste mês. Um pequeno valor já coloca sua conquista em movimento.";
        };
    }

    private String getGoalConsistencyStyle(
            SavingsGoalConsistencyStatus status
    ) {
        return switch (status) {
            case NO_CONTRIBUTIONS ->
                    "dashboard-goal-consistency-neutral";

            case STARTED_THIS_MONTH ->
                    "dashboard-goal-consistency-started";

            case CONSISTENT ->
                    "dashboard-goal-consistency-positive";

            case PAUSED ->
                    "dashboard-goal-consistency-warning";
        };
    }

    private String formatContributionWord(int amount) {
        return amount == 1
                ? "contribuição registrada"
                : "contribuições registradas";
    }

    private String formatMonthWord(int amount) {
        return amount == 1
                ? "mês"
                : "meses";
    }

    private void setGoalConsistencyVisible(
            boolean visible
    ) {
        dashboardGoalConsistencySeparator.setVisible(visible);
        dashboardGoalConsistencySeparator.setManaged(visible);

        dashboardGoalConsistencyLabel.setVisible(visible);
        dashboardGoalConsistencyLabel.setManaged(visible);

        dashboardGoalConsistencyMessageLabel.setVisible(visible);
        dashboardGoalConsistencyMessageLabel.setManaged(visible);
    }

    private void renderGoalMonthlyPace(
            SavingsGoal goal
    ) {
        try {
            SavingsGoalTargetPlan targetPlan =
                    savingsGoalTargetPlanService.calculate(
                            goal,
                            LocalDate.now()
                    );

            List<SavingsGoalContribution> contributions =
                    savingsGoalContributionRepository.findAll();

            SavingsGoalMonthlyPace pace =
                    savingsGoalMonthlyPaceService.calculate(
                            targetPlan,
                            contributions,
                            LocalDate.now()
                    );

            switch (pace.status()) {
                case NOT_STARTED ->
                        renderGoalMonthlyPaceNotStarted(pace);

                case IN_PROGRESS ->
                        renderGoalMonthlyPaceInProgress(pace);

                case MONTHLY_TARGET_REACHED ->
                        renderGoalMonthlyPaceReached(pace);

                case NO_ACTIVE_PLAN,
                     GOAL_COMPLETED ->
                        hideGoalMonthlyPace();
            }

        } catch (IllegalStateException exception) {
            hideGoalMonthlyPace();
        }
    }

    private void renderGoalMonthlyPaceNotStarted(
            SavingsGoalMonthlyPace pace
    ) {
        dashboardGoalMonthlyPaceLabel.setText(
                currencyFormat.format(BigDecimal.ZERO)
                        + " de "
                        + currencyFormat.format(
                        pace.requiredMonthlyAmount()
                )
                        + " neste mês"
        );

        dashboardGoalMonthlyPaceMessageLabel.setText(
                "Comece registrando uma contribuição para acompanhar o plano."
        );

        showGoalMonthlyPace(
                "dashboard-goal-monthly-pace-warning"
        );
    }

    private void renderGoalMonthlyPaceInProgress(
            SavingsGoalMonthlyPace pace
    ) {
        dashboardGoalMonthlyPaceLabel.setText(
                currencyFormat.format(
                        pace.contributedThisMonth()
                )
                        + " de "
                        + currencyFormat.format(
                        pace.requiredMonthlyAmount()
                )
                        + " neste mês"
        );

        dashboardGoalMonthlyPaceMessageLabel.setText(
                "Faltam "
                        + currencyFormat.format(
                        pace.remainingThisMonth()
                )
                        + " para acompanhar o plano mensal."
        );

        showGoalMonthlyPace(
                "dashboard-goal-monthly-pace-progress"
        );
    }

    private void renderGoalMonthlyPaceReached(
            SavingsGoalMonthlyPace pace
    ) {
        dashboardGoalMonthlyPaceLabel.setText(
                "Meta mensal alcançada"
        );

        dashboardGoalMonthlyPaceMessageLabel.setText(
                currencyFormat.format(
                        pace.contributedThisMonth()
                )
                        + " guardados neste mês."
        );

        showGoalMonthlyPace(
                "dashboard-goal-monthly-pace-completed"
        );
    }

    private void showGoalMonthlyPace(
            String statusStyle
    ) {
        dashboardGoalMonthlyPaceLabel
                .getStyleClass()
                .setAll(
                        "dashboard-goal-monthly-pace",
                        statusStyle
                );

        dashboardGoalMonthlyPaceLabel.setVisible(true);
        dashboardGoalMonthlyPaceLabel.setManaged(true);

        dashboardGoalMonthlyPaceMessageLabel.setVisible(true);
        dashboardGoalMonthlyPaceMessageLabel.setManaged(true);
    }

    private void hideGoalMonthlyPace() {
        dashboardGoalMonthlyPaceLabel.setText("");
        dashboardGoalMonthlyPaceMessageLabel.setText("");

        dashboardGoalMonthlyPaceLabel.setVisible(false);
        dashboardGoalMonthlyPaceLabel.setManaged(false);

        dashboardGoalMonthlyPaceMessageLabel.setVisible(false);
        dashboardGoalMonthlyPaceMessageLabel.setManaged(false);
    }

    private void renderGoalTargetPlan(
            SavingsGoal goal
    ) {
        SavingsGoalTargetPlan plan =
                savingsGoalTargetPlanService.calculate(
                        goal,
                        LocalDate.now()
                );

        switch (plan.status()) {
            case ACTIVE -> {
                dashboardGoalTargetPlanLabel.setText(
                        currencyFormat.format(
                                plan.requiredMonthlyAmount()
                        ) + " por mês"
                );

                String monthText =
                        plan.monthsAvailable() == 1
                                ? "1 mês disponível"
                                : plan.monthsAvailable()
                                  + " meses disponíveis";

                dashboardGoalTargetPlanMessageLabel.setText(
                        monthText
                                + " até a data desejada."
                );

                showGoalTargetPlan(
                        "dashboard-goal-target-plan-active"
                );
            }

            case DEADLINE_PASSED -> {
                dashboardGoalTargetPlanLabel.setText(
                        "Atualize sua data desejada"
                );

                dashboardGoalTargetPlanMessageLabel.setText(
                        "O prazo definido para este objetivo já passou."
                );

                showGoalTargetPlan(
                        "dashboard-goal-target-plan-warning"
                );
            }

            case COMPLETED -> {
                dashboardGoalTargetPlanLabel.setText(
                        "Objetivo alcançado"
                );

                dashboardGoalTargetPlanMessageLabel.setText(
                        "O valor necessário para esta conquista foi completado."
                );

                showGoalTargetPlan(
                        "dashboard-goal-target-plan-completed"
                );
            }

            case NO_TARGET_DATE ->
                    hideGoalTargetPlan();
        }
    }

    private void showGoalTargetPlan(
            String statusStyle
    ) {
        dashboardGoalTargetPlanLabel
                .getStyleClass()
                .setAll(
                        "dashboard-goal-target-plan",
                        statusStyle
                );

        dashboardGoalTargetPlanLabel.setVisible(true);
        dashboardGoalTargetPlanLabel.setManaged(true);

        dashboardGoalTargetPlanMessageLabel.setVisible(true);
        dashboardGoalTargetPlanMessageLabel.setManaged(true);

        dashboardGoalTargetPlanSeparator.setVisible(true);
        dashboardGoalTargetPlanSeparator.setManaged(true);
    }

    private void hideGoalTargetPlan() {
        dashboardGoalTargetPlanLabel.setText("");
        dashboardGoalTargetPlanMessageLabel.setText("");

        dashboardGoalTargetPlanLabel.setVisible(false);
        dashboardGoalTargetPlanLabel.setManaged(false);

        dashboardGoalTargetPlanMessageLabel.setVisible(false);
        dashboardGoalTargetPlanMessageLabel.setManaged(false);

        dashboardGoalTargetPlanSeparator.setVisible(false);
        dashboardGoalTargetPlanSeparator.setManaged(false);
    }

    private void renderGoalForecast(
            SavingsGoal goal
    ) {
        dashboardGoalForecastLabel.setVisible(true);
        dashboardGoalForecastLabel.setManaged(true);
        try {
            List<SavingsGoalContribution> contributions =
                    savingsGoalContributionRepository.findAll();

            SavingsGoalForecast forecast =
                    savingsGoalForecastService.calculate(
                            goal,
                            contributions,
                            LocalDate.now()
                    );

            dashboardGoalForecastLabel.setText(
                    forecast.message()
            );

            if (forecast.status()
                    == SavingsGoalForecastStatus.ESTIMATED_DATE) {

                forecast.completionDate().ifPresentOrElse(
                        this::showGoalForecastDate,
                        this::hideGoalForecastDate
                );

            } else {
                hideGoalForecastDate();
            }

        } catch (IllegalStateException exception) {
            dashboardGoalForecastLabel.setText(
                    "A previsão ficará disponível quando o histórico puder ser carregado."
            );

            hideGoalForecastDate();
        }
    }

    private String formatGoalShortcutProgress(
            SavingsGoalProgress progress
    ) {
        return formatGoalPercentage(
                progress.percentage()
        )
                + " concluídos. "
                + currencyFormat.format(
                progress.savedAmount()
        )
                + " guardados.";
    }

    private void showGoalForecastDate(
            LocalDate estimatedDate
    ) {
        String formattedDate = estimatedDate
                .format(goalForecastDateFormat);

        dashboardGoalForecastDateLabel.setText(
                "Previsão atual: " + formattedDate
        );

        dashboardGoalForecastDateLabel.setVisible(true);
        dashboardGoalForecastDateLabel.setManaged(true);
    }

    private void hideGoalForecastDate() {
        dashboardGoalForecastDateLabel.setText("");
        dashboardGoalForecastDateLabel.setVisible(false);
        dashboardGoalForecastDateLabel.setManaged(false);
    }

    private void renderLastGoalContribution() {
        try {
            List<SavingsGoalContribution> contributions =
                    savingsGoalContributionRepository.findAll();

            if (contributions.isEmpty()) {
                hideLastGoalContribution();
                return;
            }

            SavingsGoalContribution contribution =
                    contributions.getFirst();

            String formattedDate =
                    contribution.contributedAt()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy",
                                            Locale.of("pt", "BR")
                                    )
                            );

            dashboardGoalLastContributionLabel.setText(
                    "Última contribuição: "
                            + currencyFormat.format(
                            contribution.amount()
                    )
                            + " em "
                            + formattedDate
            );

            dashboardGoalLastContributionLabel.setVisible(true);
            dashboardGoalLastContributionLabel.setManaged(true);

        } catch (IllegalStateException exception) {
            hideLastGoalContribution();
        }
    }

    private void hideLastGoalContribution() {
        dashboardGoalLastContributionLabel.setText("");
        dashboardGoalLastContributionLabel.setVisible(false);
        dashboardGoalLastContributionLabel.setManaged(false);
    }

    @FXML
    private void showAllSmartAlerts() {
        dashboardSmartAlertsSection.fireEvent(
                new NavigationRequestEvent(
                        NavigationTarget.ALERT_CENTER
                )
        );
    }

    private void loadSmartAlerts() {
        try {
            List<DashboardSmartAlertView> alerts =
                    smartAlertPresentationService.prepare(
                            smartAlertSnoozeService.filterVisible(
                                    smartAlertService.generateAlerts()
                            ),
                            SMART_ALERT_LIMIT
                    );

            renderSmartAlerts(alerts);

        } catch (RuntimeException exception) {
            hideSmartAlerts();
        }
    }

    private void renderSmartAlerts(
            List<DashboardSmartAlertView> alerts
    ) {
        dashboardSmartAlertsContainer
                .getChildren()
                .clear();

        if (alerts.isEmpty()) {
            hideSmartAlerts();
            return;
        }

        alerts.stream()
                .map(this::createSmartAlertCard)
                .forEach(card ->
                        dashboardSmartAlertsContainer
                                .getChildren()
                                .add(card)
                );

        dashboardSmartAlertsSection.setVisible(true);
        dashboardSmartAlertsSection.setManaged(true);
    }

    private VBox createSmartAlertCard(
            DashboardSmartAlertView alert
    ) {
        Label titleLabel = new Label(alert.title());
        titleLabel.setWrapText(true);
        titleLabel.setMinWidth(0);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        titleLabel.getStyleClass().add(
                "dashboard-smart-alert-title"
        );

        Label messageLabel = new Label(alert.message());
        messageLabel.setWrapText(true);
        messageLabel.setMinWidth(0);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        messageLabel.getStyleClass().add(
                "dashboard-smart-alert-message"
        );

        Label explanationLabel = new Label(
                alert.explanation()
        );

        explanationLabel.setWrapText(true);
        explanationLabel.setMinWidth(0);
        explanationLabel.setMaxWidth(Double.MAX_VALUE);
        explanationLabel.setVisible(false);
        explanationLabel.setManaged(false);

        explanationLabel.getStyleClass().add(
                "dashboard-smart-alert-explanation"
        );

        Button explanationButton = new Button(
                "Entender este alerta"
        );

        explanationButton.getStyleClass().add(
                "dashboard-smart-alert-explanation-button"
        );

        explanationButton.setOnAction(event -> {
            boolean showExplanation =
                    !explanationLabel.isManaged();

            explanationLabel.setVisible(showExplanation);
            explanationLabel.setManaged(showExplanation);

            explanationButton.setText(
                    showExplanation
                            ? "Ocultar explicação"
                            : "Entender este alerta"
            );

            event.consume();
        });

        explanationButton.setOnMouseClicked(
                event -> event.consume()
        );

        Button snoozeButton = new Button(
                "Lembrar depois"
        );

        snoozeButton.getStyleClass().add(
                "dashboard-smart-alert-snooze-button"
        );

        snoozeButton.setOnAction(event -> {
            smartAlertSnoozeService.snooze(
                    alert.code(),
                    Duration.ofHours(24)
            );

            loadSmartAlerts();
            event.consume();
        });

        snoozeButton.setOnMouseClicked(
                event -> event.consume()
        );

        HBox alertButtons = new HBox(
                8,
                explanationButton,
                snoozeButton
        );

        alertButtons.getStyleClass().add(
                "dashboard-smart-alert-buttons"
        );

        VBox card = new VBox(
                6,
                titleLabel,
                messageLabel,
                alertButtons,
                explanationLabel
        );

        card.setPrefWidth(0);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);

        card.getStyleClass().addAll(
                "dashboard-smart-alert-card",
                alert.styleClass()
        );

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        if (alert.hasNavigation()) {
            Region spacer = new Region();

            VBox.setVgrow(
                    spacer,
                    Priority.ALWAYS
            );

            Label actionLabel = new Label(
                    getSmartAlertActionText(
                            alert.navigationTarget()
                    ) + "  →"
            );

            actionLabel.getStyleClass().add(
                    "dashboard-smart-alert-action"
            );

            card.getChildren().addAll(
                    spacer,
                    actionLabel
            );
        }

        configureSmartAlertNavigation(
                card,
                alert
        );

        return card;
    }

    private String getSmartAlertActionText(
            NavigationTarget target
    ) {
        return switch (target) {
            case FINANCIAL_PROFILE ->
                    "Abrir perfil financeiro";

            case SAVINGS_GOAL ->
                    "Ver meu objetivo";

            case HISTORY ->
                    "Revisar histórico";

            case INSIGHTS ->
                    "Ver insights";

            case NONE ->
                    "";

            default ->
                    "Abrir detalhes";
        };
    }

    private void configureSmartAlertNavigation(
            VBox card,
            DashboardSmartAlertView alert
    ) {
        if (!alert.hasNavigation()) {
            return;
        }

        card.getStyleClass().add(
                "dashboard-smart-alert-clickable"
        );

        card.setFocusTraversable(true);
        card.setAccessibleRole(
                AccessibleRole.BUTTON
        );

        card.setAccessibleText(
                alert.title()
                        + ". "
                        + alert.message()
        );

        card.setOnMouseClicked(event ->
                requestNavigation(alert)
        );

        card.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    || event.getCode() == KeyCode.SPACE) {
                requestNavigation(alert);
                event.consume();
            }
        });
    }

    private void requestNavigation(
            DashboardSmartAlertView alert
    ) {
        if (!alert.hasNavigation()) {
            return;
        }

        requestNavigation(
                alert.navigationTarget()
        );
    }

    private void requestNavigation(
            NavigationTarget target
    ) {
        if (target == NavigationTarget.NONE) {
            return;
        }

        dashboardSmartAlertsContainer.fireEvent(
                new NavigationRequestEvent(target)
        );
    }

    private void hideSmartAlerts() {
        dashboardSmartAlertsContainer
                .getChildren()
                .clear();

        dashboardSmartAlertsSection.setVisible(false);
        dashboardSmartAlertsSection.setManaged(false);
    }

}