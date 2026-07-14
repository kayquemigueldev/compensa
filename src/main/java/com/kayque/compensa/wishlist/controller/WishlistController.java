package com.kayque.compensa.wishlist.controller;

import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import com.kayque.compensa.purchase.repository.SqlitePurchaseDecisionRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class WishlistController {

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
    private Label wishlistSummaryLabel;

    @FXML
    private Label wishlistFeedbackLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private ListView<PurchaseDecisionHistoryItem> wishlistListView;

    @FXML
    private void initialize() {
        configureListView();
        loadWaitingDecisions();
    }

    private void configureListView() {
        wishlistListView.setFocusTraversable(false);

        wishlistListView.setCellFactory(
                listView -> new WishlistListCell()
        );
    }

    private void loadWaitingDecisions() {
        try {
            List<PurchaseDecisionHistoryItem> decisions =
                    repository.findWaiting();

            wishlistListView.getItems().setAll(decisions);

            boolean empty = decisions.isEmpty();

            wishlistListView.setVisible(!empty);
            wishlistListView.setManaged(!empty);

            emptyStateLabel.setVisible(empty);
            emptyStateLabel.setManaged(empty);

            wishlistSummaryLabel.setText(
                    createSummary(decisions.size())
            );

        } catch (IllegalStateException exception) {
            showError(
                    "Não foi possível carregar a lista de desejos."
            );
        }
    }

    private void finalizeDecision(
            PurchaseDecisionHistoryItem item,
            PurchaseDecisionOutcome outcome
    ) {
        try {
            boolean updated =
                    repository.finalizeWaitingDecision(
                            item.id(),
                            outcome
                    );

            if (!updated) {
                showError(
                        "Essa decisão já havia sido finalizada."
                );

                loadWaitingDecisions();
                return;
            }

            showSuccess(
                    createSuccessMessage(item, outcome)
            );

            loadWaitingDecisions();

        } catch (IllegalArgumentException |
                 IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private String createSuccessMessage(
            PurchaseDecisionHistoryItem item,
            PurchaseDecisionOutcome outcome
    ) {
        return switch (outcome) {
            case PURCHASED ->
                    item.productName()
                            + " foi marcado como comprado.";

            case DECLINED ->
                    item.productName()
                            + " foi removido da espera.";

            case WAITING ->
                    throw new IllegalArgumentException(
                            "A decisão ainda está aguardando."
                    );
        };
    }

    private String createSummary(int total) {
        if (total == 0) {
            return "Nenhuma compra aguardando decisão.";
        }

        if (total == 1) {
            return "1 compra aguardando sua decisão.";
        }

        return total + " compras aguardando sua decisão.";
    }

    private void showSuccess(String message) {
        wishlistFeedbackLabel.setText(message);

        wishlistFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-success"
        );
    }

    private void showError(String message) {
        wishlistFeedbackLabel.setText(message);

        wishlistFeedbackLabel.getStyleClass().setAll(
                "feedback-label",
                "feedback-error"
        );
    }

    private final class WishlistListCell
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
            setGraphic(createWishlistCard(item));
        }
    }

    private HBox createWishlistCard(
            PurchaseDecisionHistoryItem item
    ) {
        Label productLabel = new Label(item.productName());
        productLabel.getStyleClass().add(
                "wishlist-product"
        );

        Label dateLabel = new Label(
                "Aguardando desde "
                        + formatDate(item)
        );
        dateLabel.getStyleClass().add(
                "wishlist-date"
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
                "wishlist-value"
        );

        Label timeLabel = new Label(
                formatWorkTime(item.realWorkMinutes())
        );
        timeLabel.getStyleClass().add(
                "wishlist-value"
        );

        VBox priceBox = createInformationBox(
                "Preço",
                priceLabel
        );

        VBox timeBox = createInformationBox(
                "Tempo real",
                timeLabel
        );

        Button purchasedButton = new Button("Comprei");
        purchasedButton.getStyleClass().addAll(
                "wishlist-action",
                "wishlist-purchased"
        );

        purchasedButton.setOnAction(
                event -> finalizeDecision(
                        item,
                        PurchaseDecisionOutcome.PURCHASED
                )
        );

        Button declinedButton = new Button("Desisti");
        declinedButton.getStyleClass().addAll(
                "wishlist-action",
                "wishlist-declined"
        );

        declinedButton.setOnAction(
                event -> finalizeDecision(
                        item,
                        PurchaseDecisionOutcome.DECLINED
                )
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(
                18,
                identification,
                spacer,
                priceBox,
                timeBox,
                purchasedButton,
                declinedButton
        );

        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("wishlist-card");

        return card;
    }

    private VBox createInformationBox(
            String title,
            Label value
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(
                "wishlist-small-label"
        );

        VBox box = new VBox(
                4,
                titleLabel,
                value
        );

        box.setMinWidth(90);

        return box;
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
}