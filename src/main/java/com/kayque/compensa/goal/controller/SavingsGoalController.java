package com.kayque.compensa.goal.controller;

import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalContribution;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalProgressStatus;
import com.kayque.compensa.goal.model.SavingsGoalForecast;
import com.kayque.compensa.goal.model.SavingsGoalForecastStatus;
import com.kayque.compensa.goal.service.SavingsGoalForecastService;
import com.kayque.compensa.goal.model.SavingsGoalAchievement;

import com.kayque.compensa.goal.repository.SavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalRepository;
import com.kayque.compensa.goal.repository.SavingsGoalLifecycleRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalLifecycleRepository;
import com.kayque.compensa.goal.repository.SavingsGoalAchievementRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalAchievementRepository;

import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import com.kayque.compensa.goal.service.SavingsGoalTargetPlanService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import com.kayque.compensa.goal.model.SavingsGoalMilestone;
import com.kayque.compensa.goal.repository.SavingsGoalMilestoneRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalMilestoneRepository;
import com.kayque.compensa.goal.service.SavingsGoalMilestoneService;
import com.kayque.compensa.goal.service.SavingsGoalMonthlyPaceService;
import com.kayque.compensa.goal.model.SavingsGoalDeadlineStatus;
import com.kayque.compensa.goal.service.SavingsGoalDeadlineService;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlanStatus;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPace;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPaceStatus;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;

public class SavingsGoalController {

    private static final int HISTORY_LIMIT = 5;

    private static final int ACHIEVEMENT_HISTORY_LIMIT = 5;

    private final SavingsGoalRepository repository =
            new SqliteSavingsGoalRepository();

    private final SavingsGoalContributionRepository
            contributionRepository =
            new SqliteSavingsGoalContributionRepository();

    private final SavingsGoalLifecycleRepository
            lifecycleRepository =
            new SqliteSavingsGoalLifecycleRepository();

    private final SavingsGoalAchievementRepository
            achievementRepository =
            new SqliteSavingsGoalAchievementRepository();

    private final SavingsGoalProgressService progressService =
            new SavingsGoalProgressService();

    private final SavingsGoalMilestoneRepository
            milestoneRepository =
            new SqliteSavingsGoalMilestoneRepository();

    private final SavingsGoalMilestoneService
            milestoneService =
            new SavingsGoalMilestoneService();

    private final SavingsGoalForecastService forecastService =
            new SavingsGoalForecastService();

    private final SavingsGoalDeadlineService
            deadlineService =
            new SavingsGoalDeadlineService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy 'às' HH:mm"
            );

    private final SavingsGoalTargetPlanService
            targetPlanService =
            new SavingsGoalTargetPlanService();

    private final SavingsGoalMonthlyPaceService
            monthlyPaceService =
            new SavingsGoalMonthlyPaceService();

    private SavingsGoal currentGoal;

    private List<SavingsGoalContribution>
            currentContributions = List.of();

    @FXML
    private TextField goalNameField;

    @FXML
    private TextField targetAmountField;

    @FXML
    private TextField savedAmountField;

    @FXML
    private TextField contributionAmountField;

    @FXML
    private Button addContributionButton;

    @FXML
    private Label goalTitleLabel;

    @FXML
    private VBox targetPlanCard;

    @FXML
    private Label targetPlanTitleLabel;

    @FXML
    private Label targetPlanAmountLabel;

    @FXML
    private Label targetPlanMessageLabel;

    @FXML
    private VBox monthlyPaceSection;

    @FXML
    private Label monthlyPaceTitleLabel;

    @FXML
    private Label monthlyPaceAmountLabel;

    @FXML
    private Label monthlyPaceMessageLabel;

    @FXML
    private Label savedAmountLabel;

    @FXML
    private Label remainingAmountLabel;

    @FXML
    private Label progressPercentageLabel;

    @FXML
    private Label progressStatusLabel;

    @FXML
    private ProgressBar goalProgressBar;

    @FXML
    private Label goalFeedbackLabel;

    @FXML
    private VBox contributionHistoryContainer;

    @FXML
    private Label contributionEmptyLabel;

    @FXML
    private VBox achievementHistoryCard;

    @FXML
    private VBox achievementHistoryContainer;

    @FXML
    private Label achievementHistoryEmptyLabel;

    @FXML
    private Label forecastTitleLabel;

    @FXML
    private Label forecastMessageLabel;

    @FXML
    private Label forecastDateLabel;

    @FXML
    private VBox completedGoalCard;

    @FXML
    private Button startNewGoalButton;

    @FXML
    private DatePicker targetDatePicker;

    @FXML
    private void initialize() {
        loadGoal();
        loadContributions();
        loadAchievements();
    }

    @FXML
    private void saveGoal() {
        clearFeedback();

        try {
            BigDecimal savedAmount =
                    currentGoal == null
                            ? parseOptionalMoney(
                            savedAmountField.getText()
                    )
                            : currentGoal.savedAmount();

            LocalDate targetDate =
                    targetDatePicker.getValue();

            if (targetDate != null
                    && targetDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "A data desejada não pode estar no passado."
                );
            }

            SavingsGoal goal = new SavingsGoal(
                    goalNameField.getText(),

                    parseRequiredMoney(
                            targetAmountField.getText(),
                            "Informe o valor necessário para o objetivo."
                    ),

                    savedAmount,

                    targetDate
            );

            repository.save(goal);
            currentGoal = goal;

            fillFields(goal);
            renderGoal(goal);

            showSuccess(
                    "Objetivo salvo com sucesso."
            );

        } catch (IllegalArgumentException
                 | NullPointerException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível salvar o objetivo."
            );
        }
    }

    @FXML
    private void addContribution() {
        clearFeedback();

        try {
            if (currentGoal == null) {
                throw new IllegalArgumentException(
                        "Crie e salve um objetivo antes de adicionar uma contribuição."
                );
            }

            BigDecimal amount = parseRequiredMoney(
                    contributionAmountField.getText(),
                    "Informe o valor da contribuição."
            );

            contributionRepository.add(amount);

            contributionAmountField.clear();

            reloadGoal();
            loadContributions();

            showSuccess(
                    "Contribuição registrada com sucesso."
            );

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível registrar a contribuição."
            );
        }
    }

    @FXML
    private void startNewGoal() {
        clearFeedback();

        try {
            if (currentGoal == null) {
                throw new IllegalArgumentException(
                        "Nenhum objetivo foi encontrado."
                );
            }

            if (!currentGoal.isCompleted()) {
                throw new IllegalArgumentException(
                        "Conclua o objetivo atual antes de iniciar outro."
                );
            }

            ButtonType confirmButton = new ButtonType(
                    "Arquivar e continuar",
                    ButtonBar.ButtonData.OK_DONE
            );

            ButtonType cancelButton = new ButtonType(
                    "Cancelar",
                    ButtonBar.ButtonData.CANCEL_CLOSE
            );

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "",
                    confirmButton,
                    cancelButton
            );

            confirmation.setTitle(
                    "Começar um novo objetivo"
            );

            confirmation.setHeaderText(
                    "Arquivar esta conquista?"
            );

            confirmation.setContentText(
                    "O objetivo “"
                            + currentGoal.name()
                            + "” e suas contribuições serão preservados "
                            + "no histórico.\n\n"
                            + "Depois disso, você poderá planejar uma nova "
                            + "conquista. Os demais dados do Compensa? "
                            + "não serão apagados."
            );

            ButtonType selectedButton =
                    confirmation.showAndWait()
                            .orElse(cancelButton);

            if (selectedButton != confirmButton) {
                return;
            }

            String completedGoalName =
                    currentGoal.name();

            lifecycleRepository
                    .archiveCompletedGoalAndPrepareNew();

            currentGoal = null;
            currentContributions = List.of();

            loadGoal();
            loadContributions();

            showSuccess(
                    "Conquista “"
                            + completedGoalName
                            + "” arquivada com sucesso. "
                            + "Agora planeje seu próximo objetivo."
            );

        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível arquivar a conquista."
            );
        }
    }

    private void loadGoal() {
        clearFeedback();

        try {
            repository.find().ifPresentOrElse(
                    goal -> {
                        currentGoal = goal;
                        fillFields(goal);
                        renderGoal(goal);
                        setContributionFormEnabled(
                                !goal.isCompleted()
                        );
                    },
                    () -> {
                        currentGoal = null;
                        renderEmptyState();
                        setContributionFormEnabled(false);
                    }
            );

        } catch (IllegalStateException exception) {
            currentGoal = null;
            renderEmptyState();
            setContributionFormEnabled(false);

            showError(
                    "Não foi possível carregar o objetivo."
            );
        }
    }

    private void reloadGoal() {
        currentGoal = repository.find()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "O objetivo não foi encontrado."
                        )
                );

        fillFields(currentGoal);
        renderGoal(currentGoal);
    }

    private void fillFields(SavingsGoal goal) {
        goalNameField.setText(goal.name());
        targetDatePicker.setValue(
                goal.targetDate()
        );

        targetAmountField.setText(
                currencyFormat.format(
                        goal.targetAmount()
                )
        );

        savedAmountField.setText(
                currencyFormat.format(
                        goal.savedAmount()
                )
        );

        /*
         * Depois que o objetivo existe, o saldo só pode ser
         * alterado por contribuições registradas.
         */
        savedAmountField.setDisable(true);
    }

    private void renderGoal(SavingsGoal goal) {
        SavingsGoalProgress progress =
                progressService.calculate(goal);

        goalTitleLabel.setText(goal.name());

        savedAmountLabel.setText(
                currencyFormat.format(
                        progress.savedAmount()
                )
        );

        remainingAmountLabel.setText(
                currencyFormat.format(
                        progress.remainingAmount()
                )
        );

        progressPercentageLabel.setText(
                formatPercentage(
                        progress.percentage()
                )
        );

        goalProgressBar.setProgress(
                progress.percentage()
                        .divide(new BigDecimal("100"))
                        .doubleValue()
        );

        renderStatus(progress.status());
        celebrateNewMilestone(progress.percentage());

        boolean completed = goal.isCompleted();

        setCompletedGoalCardVisible(completed);
        setContributionFormEnabled(!completed);
    }

    private void celebrateNewMilestone(
            BigDecimal currentPercentage
    ) {
        try {
            int lastCelebrated =
                    milestoneRepository
                            .findLastCelebratedMilestone();

            milestoneService.findNewMilestone(
                    currentPercentage,
                    lastCelebrated
            ).ifPresent(this::showMilestoneCelebration);

        } catch (IllegalStateException exception) {
            // A tela continua funcionando mesmo se a celebração falhar.
        }
    }

    private void showMilestoneCelebration(
            SavingsGoalMilestone milestone
    ) {
        milestoneRepository.saveLastCelebratedMilestone(
                milestone.percentage()
        );

        progressStatusLabel.setText(
                milestone.message()
        );

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                "goal-milestone-celebration"
        );

        progressStatusLabel.setOpacity(0);
        progressStatusLabel.setScaleX(0.92);
        progressStatusLabel.setScaleY(0.92);

        FadeTransition fade = new FadeTransition(
                Duration.millis(650),
                progressStatusLabel
        );

        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(
                Duration.millis(650),
                progressStatusLabel
        );

        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition celebration =
                new ParallelTransition(fade, scale);

        celebration.play();
    }

    private void renderStatus(
            SavingsGoalProgressStatus status
    ) {
        String text = switch (status) {
            case NOT_STARTED ->
                    "Seu objetivo está pronto. Registre sua primeira contribuição.";

            case IN_PROGRESS ->
                    "Você já começou. Cada contribuição aproxima você da sua conquista.";

            case COMPLETED ->
                    "Objetivo alcançado! Você chegou ao valor planejado.";
        };

        progressStatusLabel.setText(text);

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                getStatusStyle(status)
        );
    }

    private String getStatusStyle(
            SavingsGoalProgressStatus status
    ) {
        return switch (status) {
            case NOT_STARTED -> "goal-status-warning";
            case IN_PROGRESS -> "goal-status-progress";
            case COMPLETED -> "goal-status-completed";
        };
    }

    private void loadContributions() {
        try {
            currentContributions =
                    contributionRepository.findAll();

            renderContributionHistory(
                    currentContributions
            );

            renderForecast();

        } catch (IllegalStateException exception) {
            currentContributions = List.of();

            contributionHistoryContainer
                    .getChildren()
                    .clear();

            contributionEmptyLabel.setText(
                    "Não foi possível carregar as contribuições."
            );

            contributionEmptyLabel.setVisible(true);
            contributionEmptyLabel.setManaged(true);

            renderForecastUnavailable();
        }
    }

    private void renderTargetPlan() {
        if (currentGoal == null) {
            hideTargetPlan();
            return;
        }

        SavingsGoalTargetPlan plan =
                targetPlanService.calculate(
                        currentGoal,
                        LocalDate.now()
                );

        switch (plan.status()) {
            case ACTIVE -> {
                renderActiveTargetPlan(plan);
                renderMonthlyPace(plan);
            }

            case NO_TARGET_DATE -> {
                hideMonthlyPace();
                hideTargetPlan();
            }

            case DEADLINE_PASSED -> {
                hideMonthlyPace();
                renderExpiredTargetPlan(plan);
            }

            case COMPLETED -> {
                hideMonthlyPace();
                renderCompletedTargetPlan();
            }
        }
    }

    private void renderMonthlyPace(
            SavingsGoalTargetPlan targetPlan
    ) {
        SavingsGoalMonthlyPace pace =
                monthlyPaceService.calculate(
                        targetPlan,
                        currentContributions,
                        LocalDate.now()
                );

        monthlyPaceTitleLabel.setText(
                "Seu ritmo neste mês"
        );

        switch (pace.status()) {
            case NOT_STARTED ->
                    renderMonthlyPaceNotStarted(pace);

            case IN_PROGRESS ->
                    renderMonthlyPaceInProgress(pace);

            case MONTHLY_TARGET_REACHED ->
                    renderMonthlyPaceReached(pace);

            case NO_ACTIVE_PLAN,
                 GOAL_COMPLETED ->
                    hideMonthlyPace();
        }
    }

    private void renderMonthlyPaceNotStarted(
            SavingsGoalMonthlyPace pace
    ) {
        monthlyPaceAmountLabel.setText(
                currencyFormat.format(BigDecimal.ZERO)
                        + " de "
                        + currencyFormat.format(
                        pace.requiredMonthlyAmount()
                )
        );

        monthlyPaceMessageLabel.setText(
                "Comece registrando uma contribuição. "
                        + currencyFormat.format(
                        pace.remainingThisMonth()
                )
                        + " ainda precisam ser guardados neste mês."
        );

        showMonthlyPace(
                "goal-monthly-pace-warning"
        );
    }

    private void renderMonthlyPaceInProgress(
            SavingsGoalMonthlyPace pace
    ) {
        monthlyPaceAmountLabel.setText(
                currencyFormat.format(
                        pace.contributedThisMonth()
                )
                        + " de "
                        + currencyFormat.format(
                        pace.requiredMonthlyAmount()
                )
        );

        monthlyPaceMessageLabel.setText(
                "Faltam "
                        + currencyFormat.format(
                        pace.remainingThisMonth()
                )
                        + " para acompanhar seu plano mensal."
        );

        showMonthlyPace(
                "goal-monthly-pace-progress"
        );
    }

    private void renderMonthlyPaceReached(
            SavingsGoalMonthlyPace pace
    ) {
        monthlyPaceAmountLabel.setText(
                "Meta mensal alcançada"
        );

        monthlyPaceMessageLabel.setText(
                "Você guardou "
                        + currencyFormat.format(
                        pace.contributedThisMonth()
                )
                        + " neste mês. Excelente progresso!"
        );

        showMonthlyPace(
                "goal-monthly-pace-completed"
        );
    }

    private void showMonthlyPace(
            String statusStyle
    ) {
        monthlyPaceSection.setVisible(true);
        monthlyPaceSection.setManaged(true);

        monthlyPaceSection.getStyleClass().setAll(
                "goal-monthly-pace-section",
                statusStyle
        );
    }

    private void hideMonthlyPace() {
        monthlyPaceSection.setVisible(false);
        monthlyPaceSection.setManaged(false);

        monthlyPaceTitleLabel.setText("");
        monthlyPaceAmountLabel.setText("");
        monthlyPaceMessageLabel.setText("");
    }

    private void renderActiveTargetPlan(
            SavingsGoalTargetPlan plan
    ) {
        String monthText =
                plan.monthsAvailable() == 1
                        ? "1 mês disponível"
                        : plan.monthsAvailable()
                          + " meses disponíveis";

        targetPlanTitleLabel.setText(
                "Quanto guardar por mês"
        );

        targetPlanAmountLabel.setText(
                currencyFormat.format(
                        plan.requiredMonthlyAmount()
                ) + " por mês"
        );

        targetPlanMessageLabel.setText(
                monthText
                        + " para completar os "
                        + currencyFormat.format(
                        plan.remainingAmount()
                )
                        + " restantes."
        );

        showTargetPlan(
                "goal-target-plan-card",
                "goal-target-plan-active"
        );
    }

    private void renderExpiredTargetPlan(
            SavingsGoalTargetPlan plan
    ) {
        targetPlanTitleLabel.setText(
                "A data desejada já passou"
        );

        targetPlanAmountLabel.setText(
                currencyFormat.format(
                        plan.remainingAmount()
                ) + " restantes"
        );

        targetPlanMessageLabel.setText(
                "Escolha uma nova data para recalcular um plano mensal possível."
        );

        showTargetPlan(
                "goal-target-plan-card",
                "goal-target-plan-warning"
        );
    }

    private void renderCompletedTargetPlan() {
        targetPlanTitleLabel.setText(
                "Plano concluído"
        );

        targetPlanAmountLabel.setText(
                "Objetivo alcançado"
        );

        targetPlanMessageLabel.setText(
                "Você completou o valor necessário para esta conquista."
        );

        showTargetPlan(
                "goal-target-plan-card",
                "goal-target-plan-completed"
        );
    }

    private void showTargetPlan(
            String baseStyle,
            String statusStyle
    ) {
        targetPlanCard.setVisible(true);
        targetPlanCard.setManaged(true);

        targetPlanCard.getStyleClass().setAll(
                baseStyle,
                statusStyle
        );
    }

    private void hideTargetPlan() {
        hideMonthlyPace();

        targetPlanCard.setVisible(false);
        targetPlanCard.setManaged(false);

        targetPlanTitleLabel.setText("");
        targetPlanAmountLabel.setText("");
        targetPlanMessageLabel.setText("");
    }

    private void renderForecast() {
        if (currentGoal == null) {
            hideTargetPlan();
            renderForecastUnavailable();
            return;
        }

        renderTargetPlan();

        SavingsGoalForecast forecast =
                forecastService.calculate(
                        currentGoal,
                        currentContributions,
                        LocalDate.now()
                );

        forecastTitleLabel.setText(
                getForecastTitle(forecast.status())
        );

        forecastMessageLabel.setText(
                forecast.message()
        );

        forecastDateLabel.setText(
                forecast.completionDate()
                        .map(date -> date.format(
                                DateTimeFormatter.ofPattern(
                                        "MMMM 'de' yyyy",
                                        Locale.of("pt", "BR")
                                )
                        ))
                        .orElse("")
        );

        forecastDateLabel.setVisible(
                forecast.completionDate().isPresent()
        );

        forecastDateLabel.setManaged(
                forecast.completionDate().isPresent()
        );

        forecastMessageLabel.getStyleClass().setAll(
                "goal-forecast-message",
                getForecastStyle(forecast.status())
        );
        renderDeadlineComparison(forecast);
    }

    private void renderDeadlineComparison(
            SavingsGoalForecast forecast
    ) {
        SavingsGoalDeadlineStatus deadlineStatus =
                deadlineService.evaluate(
                        currentGoal,
                        forecast
                );

        if (deadlineStatus
                == SavingsGoalDeadlineStatus.NO_TARGET_DATE) {
            return;
        }

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );

        String targetDate = currentGoal
                .targetDate()
                .format(dateFormatter);

        switch (deadlineStatus) {
            case FORECAST_UNAVAILABLE -> {
                forecastTitleLabel.setText(
                        "Data desejada definida"
                );

                forecastMessageLabel.setText(
                        "Você deseja alcançar este objetivo até "
                                + targetDate
                                + ". Registre mais contribuições para compararmos seu ritmo."
                );

                setDeadlineStyle(
                        "goal-forecast-neutral"
                );
            }

            case AHEAD_OF_SCHEDULE -> {
                String forecastDate = forecast
                        .completionDate()
                        .orElseThrow()
                        .format(dateFormatter);

                forecastTitleLabel.setText(
                        "Você pode chegar antes do planejado"
                );

                forecastMessageLabel.setText(
                        "Mantendo esse ritmo, a previsão é "
                                + forecastDate
                                + ", antes da data desejada de "
                                + targetDate
                                + "."
                );

                setDeadlineStyle(
                        "goal-forecast-progress"
                );
            }

            case ON_SCHEDULE -> {
                forecastTitleLabel.setText(
                        "Seu ritmo está alinhado"
                );

                forecastMessageLabel.setText(
                        "A previsão atual coincide com sua data desejada: "
                                + targetDate
                                + "."
                );

                setDeadlineStyle(
                        "goal-forecast-progress"
                );
            }

            case BEHIND_SCHEDULE -> {
                String forecastDate = forecast
                        .completionDate()
                        .orElseThrow()
                        .format(dateFormatter);

                forecastTitleLabel.setText(
                        "Seu ritmo precisa aumentar"
                );

                forecastMessageLabel.setText(
                        "No ritmo atual, a previsão é "
                                + forecastDate
                                + ", depois da data desejada de "
                                + targetDate
                                + "."
                );

                setDeadlineStyle(
                        "goal-forecast-warning"
                );
            }

            case COMPLETED -> {
                forecastTitleLabel.setText(
                        "Conquista alcançada"
                );

                forecastMessageLabel.setText(
                        "Você concluiu seu objetivo. Data desejada: "
                                + targetDate
                                + "."
                );

                setDeadlineStyle(
                        "goal-forecast-completed"
                );
            }

            case NO_TARGET_DATE -> {
                // O texto padrão da previsão será mantido.
            }
        }
    }

    private void setDeadlineStyle(String styleClass) {
        forecastMessageLabel.getStyleClass().setAll(
                "goal-forecast-message",
                styleClass
        );
    }

    private void renderForecastUnavailable() {
        renderTargetPlan();

        forecastTitleLabel.setText(
                "Previsão da conquista"
        );

        forecastMessageLabel.setText(
                "Registre contribuições para gerar uma previsão."
        );

        forecastMessageLabel.getStyleClass().setAll(
                "goal-forecast-message",
                "goal-forecast-neutral"
        );

        forecastDateLabel.setText("");
        forecastDateLabel.setVisible(false);
        forecastDateLabel.setManaged(false);
    }

    private String getForecastTitle(
            SavingsGoalForecastStatus status
    ) {
        return switch (status) {
            case NO_HISTORY ->
                    "Previsão da conquista";

            case CONTRIBUTIONS_NEEDED ->
                    "Continue nesse ritmo";

            case ESTIMATED_DATE ->
                    "Quando você poderá chegar lá?";

            case COMPLETED ->
                    "Conquista alcançada";
        };
    }

    private String getForecastStyle(
            SavingsGoalForecastStatus status
    ) {
        return switch (status) {
            case NO_HISTORY ->
                    "goal-forecast-neutral";

            case CONTRIBUTIONS_NEEDED,
                 ESTIMATED_DATE ->
                    "goal-forecast-progress";

            case COMPLETED ->
                    "goal-forecast-completed";
        };
    }

    private void renderContributionHistory(
            List<SavingsGoalContribution> contributions
    ) {
        contributionHistoryContainer
                .getChildren()
                .clear();

        boolean empty = contributions.isEmpty();

        contributionEmptyLabel.setVisible(empty);
        contributionEmptyLabel.setManaged(empty);

        if (empty) {
            return;
        }

        contributions.stream()
                .limit(HISTORY_LIMIT)
                .map(this::createContributionRow)
                .forEach(row ->
                        contributionHistoryContainer
                                .getChildren()
                                .add(row)
                );
    }

    private HBox createContributionRow(
            SavingsGoalContribution contribution
    ) {
        Label dateLabel = new Label(
                contribution.contributedAt()
                        .format(dateFormat)
        );

        dateLabel.getStyleClass().add(
                "goal-contribution-date"
        );

        Label descriptionLabel = new Label(
                "Valor adicionado ao objetivo"
        );

        descriptionLabel.getStyleClass().add(
                "goal-contribution-description"
        );

        VBox information = new VBox(
                3,
                dateLabel,
                descriptionLabel
        );

        Label amountLabel = new Label(
                "+ " + currencyFormat.format(
                        contribution.amount()
                )
        );

        amountLabel.getStyleClass().add(
                "goal-contribution-value"
        );

        Button undoButton = new Button("Desfazer");

        undoButton.getStyleClass().add(
                "goal-contribution-undo-button"
        );

        undoButton.setOnAction(event ->
                undoContribution(contribution)
        );

        Region spacer = new Region();
        HBox.setHgrow(
                spacer,
                javafx.scene.layout.Priority.ALWAYS
        );

        HBox row = new HBox(
                12,
                information,
                spacer,
                amountLabel,
                undoButton
        );

        row.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        row.getStyleClass().add(
                "goal-contribution-row"
        );

        return row;
    }

    private void undoContribution(
            SavingsGoalContribution contribution
    ) {
        clearFeedback();

        try {
            contributionRepository.remove(
                    contribution.id()
            );

            loadGoal();
            loadContributions();
            loadAchievements();

            showSuccess(
                    "Contribuição de "
                            + currencyFormat.format(
                            contribution.amount()
                    )
                            + " desfeita com sucesso."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void setCompletedGoalCardVisible(
            boolean visible
    ) {
        completedGoalCard.setVisible(visible);
        completedGoalCard.setManaged(visible);

        startNewGoalButton.setDisable(!visible);
    }

    private void loadAchievements() {
        try {
            List<SavingsGoalAchievement> achievements =
                    achievementRepository.findAll();

            renderAchievementHistory(achievements);

        } catch (IllegalStateException exception) {
            achievementHistoryContainer
                    .getChildren()
                    .clear();

            achievementHistoryEmptyLabel.setText(
                    "Não foi possível carregar as conquistas anteriores."
            );

            achievementHistoryEmptyLabel.setVisible(true);
            achievementHistoryEmptyLabel.setManaged(true);
        }
    }

    private void renderAchievementHistory(
            List<SavingsGoalAchievement> achievements
    ) {
        achievementHistoryContainer
                .getChildren()
                .clear();

        boolean empty = achievements.isEmpty();

        achievementHistoryEmptyLabel.setVisible(empty);
        achievementHistoryEmptyLabel.setManaged(empty);

        if (empty) {
            return;
        }

        achievements.stream()
                .limit(ACHIEVEMENT_HISTORY_LIMIT)
                .map(this::createAchievementRow)
                .forEach(row ->
                        achievementHistoryContainer
                                .getChildren()
                                .add(row)
                );
    }

    private HBox createAchievementRow(
            SavingsGoalAchievement achievement
    ) {
        Label nameLabel = new Label(
                achievement.name()
        );

        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add(
                "goal-achievement-name"
        );

        Label completedDateLabel = new Label(
                "Concluído em "
                        + achievement.completedAt()
                        .format(dateFormat)
        );

        completedDateLabel.getStyleClass().add(
                "goal-achievement-date"
        );

        VBox information = new VBox(
                4,
                nameLabel,
                completedDateLabel
        );

        HBox.setHgrow(
                information,
                javafx.scene.layout.Priority.ALWAYS
        );

        Label savedAmountLabel = new Label(
                currencyFormat.format(
                        achievement.savedAmount()
                )
        );

        savedAmountLabel.getStyleClass().add(
                "goal-achievement-value"
        );

        Label targetAmountLabel = new Label(
                "Meta: "
                        + currencyFormat.format(
                        achievement.targetAmount()
                )
        );

        targetAmountLabel.getStyleClass().add(
                "goal-achievement-target"
        );

        VBox amounts = new VBox(
                4,
                savedAmountLabel,
                targetAmountLabel
        );

        amounts.setAlignment(
                javafx.geometry.Pos.CENTER_RIGHT
        );

        HBox row = new HBox(
                16,
                information,
                amounts
        );

        row.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        row.setMaxWidth(Double.MAX_VALUE);

        row.getStyleClass().add(
                "goal-achievement-row"
        );

        return row;
    }

    private void renderEmptyState() {
        goalNameField.clear();
        targetAmountField.clear();
        savedAmountField.clear();
        contributionAmountField.clear();

        targetDatePicker.setValue(null);

        goalTitleLabel.setText(
                "Defina sua próxima conquista"
        );

        savedAmountLabel.setText("--");
        remainingAmountLabel.setText("--");
        progressPercentageLabel.setText("0%");

        goalProgressBar.setProgress(0);

        savedAmountField.setDisable(false);

        progressStatusLabel.setText(
                "Preencha os dados ao lado para começar a acompanhar seu progresso."
        );

        progressStatusLabel.getStyleClass().setAll(
                "goal-status",
                "goal-status-warning"
        );

        currentContributions = List.of();

        setCompletedGoalCardVisible(false);
        setContributionFormEnabled(false);

        targetPlanCard.setVisible(false);
        targetPlanCard.setManaged(false);

        monthlyPaceSection.setVisible(false);
        monthlyPaceSection.setManaged(false);

        renderForecastUnavailable();
    }

    private void setContributionFormEnabled(
            boolean enabled
    ) {
        contributionAmountField.setDisable(!enabled);
        addContributionButton.setDisable(!enabled);
    }

    private BigDecimal parseRequiredMoney(
            String text,
            String emptyMessage
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    emptyMessage
            );
        }

        return parseMoney(text);
    }

    private BigDecimal parseOptionalMoney(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }

        return parseMoney(text);
    }

    private BigDecimal parseMoney(String text) {
        try {
            String normalizedValue = text
                    .trim()
                    .replace("R$", "")
                    .replace("\u00A0", "")
                    .replace("\u202F", "")
                    .replace(" ", "");

            if (normalizedValue.contains(",")) {
                normalizedValue = normalizedValue
                        .replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(normalizedValue);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Informe um valor financeiro válido."
            );
        }
    }

    private String formatPercentage(
            BigDecimal percentage
    ) {
        return percentage
                .stripTrailingZeros()
                .toPlainString()
                .replace(".", ",")
                + "%";
    }

    private void clearFeedback() {
        goalFeedbackLabel.setText("");

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label"
        );
    }

    private void showSuccess(String message) {
        goalFeedbackLabel.setText(message);

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        goalFeedbackLabel.setText(message);

        goalFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }
}