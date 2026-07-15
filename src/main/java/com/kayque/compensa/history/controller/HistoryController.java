package com.kayque.compensa.history.controller;

import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import com.kayque.compensa.purchase.repository.SqlitePurchaseDecisionRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HistoryController {

    private final PurchaseDecisionRepository repository =
            new SqlitePurchaseDecisionRepository();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.of("pt", "BR")
            );

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy 'às' HH:mm"
            );

    @FXML
    private Label historySummaryLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private ListView<PurchaseDecisionHistoryItem> historyListView;

    @FXML
    private void initialize() {
        configureListView();
        loadHistory();
    }

    private void configureListView() {
        historyListView.setFocusTraversable(false);

        historyListView.setCellFactory(
                listView -> new HistoryListCell()
        );
    }

    private void loadHistory() {
        try {
            List<PurchaseDecisionHistoryItem> history =
                    repository.findAll();

            historyListView.getItems().setAll(history);

            boolean empty = history.isEmpty();

            historyListView.setVisible(!empty);
            historyListView.setManaged(!empty);

            emptyStateLabel.setVisible(empty);
            emptyStateLabel.setManaged(empty);

            historySummaryLabel.setText(
                    createSummary(history.size())
            );

        } catch (IllegalStateException exception) {
            historyListView.setVisible(false);
            historyListView.setManaged(false);

            emptyStateLabel.setVisible(true);
            emptyStateLabel.setManaged(true);
            emptyStateLabel.setText(
                    "Não foi possível carregar o histórico."
            );

            historySummaryLabel.setText(
                    "Tente novamente mais tarde."
            );
        }
    }

    private String createSummary(int total) {
        if (total == 0) {
            return "Nenhuma decisão registrada ainda.";
        }

        if (total == 1) {
            return "1 decisão registrada.";
        }

        return total + " decisões registradas.";
    }

    private final class HistoryListCell
            extends ListCell<PurchaseDecisionHistoryItem> {

        @Override
        protected void updateItem(
                PurchaseDecisionHistoryItem item,
                boolean empty
        ) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(null);
            setGraphic(createHistoryCard(item));
        }
    }

    private Node createHistoryCard(
            PurchaseDecisionHistoryItem item
    ) {
        Label productLabel = new Label(item.productName());
        productLabel.getStyleClass().add(
                "history-product"
        );

        Label dateLabel = new Label(formatDate(item));
        dateLabel.getStyleClass().add(
                "history-date"
        );

        VBox identification = new VBox(
                5,
                productLabel,
                dateLabel
        );

        Label priceLabel = new Label(
                currencyFormat.format(item.price())
        );
        priceLabel.getStyleClass().add(
                "history-value"
        );

        Label priceTitle = createSmallLabel("Preço");

        VBox priceBox = createInformationBox(
                priceTitle,
                priceLabel
        );

        Label timeLabel = new Label(
                formatWorkTime(item.realWorkMinutes())
        );
        timeLabel.getStyleClass().add(
                "history-value"
        );

        Label timeTitle = createSmallLabel("Tempo real");

        VBox timeBox = createInformationBox(
                timeTitle,
                timeLabel
        );

        Label recommendationLabel = new Label(
                formatRecommendation(item.adviceStatus())
        );

        recommendationLabel.getStyleClass().addAll(
                "history-badge",
                getRecommendationStyle(item.adviceStatus())
        );

        Label outcomeLabel = new Label(
                formatOutcome(item.outcome())
        );

        outcomeLabel.getStyleClass().addAll(
                "history-badge",
                getOutcomeStyle(item.outcome())
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox informationRow = new HBox(
                18,
                identification,
                spacer,
                priceBox,
                timeBox,
                recommendationLabel,
                outcomeLabel
        );

        informationRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, informationRow);
        card.getStyleClass().add("history-card");

        if (item.outcome() == PurchaseDecisionOutcome.PURCHASED) {
            card.getChildren().add(
                    createSatisfactionSection(item)
            );
        }

        return card;
    }

    private Node createSatisfactionSection(
            PurchaseDecisionHistoryItem item
    ) {
        if (item.satisfaction() != null) {
            Label title = createSmallLabel(
                    "Depois da compra"
            );

            Label satisfactionLabel = new Label(
                    formatSatisfaction(item.satisfaction())
            );

            satisfactionLabel.getStyleClass().addAll(
                    "history-badge",
                    getSatisfactionStyle(item.satisfaction())
            );

            HBox evaluatedBox = new HBox(
                    12,
                    title,
                    satisfactionLabel
            );

            evaluatedBox.setAlignment(Pos.CENTER_LEFT);

            return evaluatedBox;
        }

        Label questionLabel = new Label(
                "Depois da compra: valeu a pena?"
        );

        questionLabel.getStyleClass().add(
                "history-evaluation-question"
        );

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        feedbackLabel.getStyleClass().add(
                "history-evaluation-error"
        );

        Button worthItButton = createEvaluationButton(
                "Valeu a pena",
                item,
                PurchaseSatisfaction.WORTH_IT,
                feedbackLabel
        );

        Button partiallyButton = createEvaluationButton(
                "Mais ou menos",
                item,
                PurchaseSatisfaction.PARTIALLY_WORTH_IT,
                feedbackLabel
        );

        Button regrettedButton = createEvaluationButton(
                "Me arrependi",
                item,
                PurchaseSatisfaction.REGRETTED,
                feedbackLabel
        );

        HBox buttons = new HBox(
                10,
                worthItButton,
                partiallyButton,
                regrettedButton
        );

        buttons.setAlignment(Pos.CENTER_LEFT);

        return new VBox(
                8,
                questionLabel,
                buttons,
                feedbackLabel
        );
    }

    private Button createEvaluationButton(
            String text,
            PurchaseDecisionHistoryItem item,
            PurchaseSatisfaction satisfaction,
            Label feedbackLabel
    ) {
        Button button = new Button(text);

        button.getStyleClass().add(
                "history-evaluation-button"
        );

        button.setOnAction(event -> {
            try {
                boolean updated =
                        repository.evaluatePurchasedDecision(
                                item.id(),
                                satisfaction
                        );

                if (!updated) {
                    showEvaluationError(
                            feedbackLabel,
                            "Essa compra não pôde ser avaliada."
                    );
                    return;
                }

                loadHistory();

            } catch (IllegalStateException exception) {
                showEvaluationError(
                        feedbackLabel,
                        "Não foi possível salvar a avaliação."
                );
            }
        });

        return button;
    }

    private void showEvaluationError(
            Label feedbackLabel,
            String message
    ) {
        feedbackLabel.setText(message);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    private String formatSatisfaction(
            PurchaseSatisfaction satisfaction
    ) {
        return switch (satisfaction) {
            case WORTH_IT -> "Valeu a pena";
            case PARTIALLY_WORTH_IT -> "Mais ou menos";
            case REGRETTED -> "Me arrependi";
        };
    }

    private String getSatisfactionStyle(
            PurchaseSatisfaction satisfaction
    ) {
        return switch (satisfaction) {
            case WORTH_IT -> "history-positive";
            case PARTIALLY_WORTH_IT -> "history-warning";
            case REGRETTED -> "history-negative";
        };
    }

    private VBox createInformationBox(
            Label title,
            Label value
    ) {
        VBox box = new VBox(4, title, value);
        box.setMinWidth(90);
        return box;
    }

    private Label createSmallLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("history-small-label");
        return label;
    }

    private String formatDate(
            PurchaseDecisionHistoryItem item
    ) {
        return dateFormat.format(
                item.createdAt()
                        .atZone(ZoneId.systemDefault())
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

    private String formatRecommendation(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> "Faz sentido";
            case THINK_AGAIN -> "Pense novamente";
            case PROBABLY_NOT_WORTH_IT ->
                    "Provavelmente não compensa";
        };
    }

    private String formatOutcome(
            PurchaseDecisionOutcome outcome
    ) {
        return switch (outcome) {
            case PURCHASED -> "Comprou";
            case WAITING -> "Esperando";
            case DECLINED -> "Não comprou";
        };
    }

    private String getRecommendationStyle(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> "history-positive";
            case THINK_AGAIN -> "history-warning";
            case PROBABLY_NOT_WORTH_IT ->
                    "history-negative";
        };
    }

    private String getOutcomeStyle(
            PurchaseDecisionOutcome outcome
    ) {
        return switch (outcome) {
            case PURCHASED -> "history-purchased";
            case WAITING -> "history-waiting";
            case DECLINED -> "history-declined";
        };
    }
}