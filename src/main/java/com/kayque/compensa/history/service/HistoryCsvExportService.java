package com.kayque.compensa.history.service;

import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class HistoryCsvExportService {

    private static final String SEPARATOR = ";";
    private static final String LINE_BREAK = "\r\n";

    private final ZoneId zoneId;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    public HistoryCsvExportService() {
        this(ZoneId.systemDefault());
    }

    public HistoryCsvExportService(ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(
                zoneId,
                "O fuso horário é obrigatório."
        );
    }

    public void export(
            List<PurchaseDecisionHistoryItem> history,
            Path destination
    ) throws IOException {
        Objects.requireNonNull(
                history,
                "O histórico é obrigatório."
        );

        Objects.requireNonNull(
                destination,
                "O destino do arquivo é obrigatório."
        );

        if (history.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem decisões para exportar."
            );
        }

        Files.writeString(
                destination,
                createCsv(history),
                StandardCharsets.UTF_8
        );
    }

    String createCsv(
            List<PurchaseDecisionHistoryItem> history
    ) {
        StringBuilder csv = new StringBuilder();

        // BOM ajuda Excel e Numbers a reconhecerem UTF-8.
        csv.append('\uFEFF');

        csv.append(createRow(
                "Data",
                "Compra",
                "Preço",
                "Tempo real",
                "Recomendação",
                "Decisão",
                "Satisfação"
        ));

        for (PurchaseDecisionHistoryItem item : history) {
            csv.append(createRow(
                    formatDate(item),
                    item.productName(),
                    formatMoney(item),
                    formatWorkTime(item.realWorkMinutes()),
                    formatRecommendation(item.adviceStatus()),
                    formatOutcome(item.outcome()),
                    formatSatisfaction(item.satisfaction())
            ));
        }

        return csv.toString();
    }

    private String createRow(String... values) {
        return String.join(
                SEPARATOR,
                java.util.Arrays.stream(values)
                        .map(this::escape)
                        .toList()
        ) + LINE_BREAK;
    }

    private String escape(String value) {
        String safeValue = value == null ? "" : value;

        boolean requiresQuotes =
                safeValue.contains(SEPARATOR)
                        || safeValue.contains("\"")
                        || safeValue.contains("\n")
                        || safeValue.contains("\r");

        if (!requiresQuotes) {
            return safeValue;
        }

        return "\""
                + safeValue.replace("\"", "\"\"")
                + "\"";
    }

    private String formatDate(
            PurchaseDecisionHistoryItem item
    ) {
        return item.createdAt()
                .atZone(zoneId)
                .format(dateFormatter);
    }

    private String formatMoney(
            PurchaseDecisionHistoryItem item
    ) {
        return "R$ "
                + item.price()
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                .toPlainString()
                .replace(".", ",");
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
            case WAITING -> "Aguardando decisão";
            case DECLINED -> "Não comprou";
        };
    }

    private String formatSatisfaction(
            PurchaseSatisfaction satisfaction
    ) {
        if (satisfaction == null) {
            return "Não informada";
        }

        return switch (satisfaction) {
            case WORTH_IT -> "Valeu a pena";
            case PARTIALLY_WORTH_IT -> "Mais ou menos";
            case REGRETTED -> "Arrependimento";
        };
    }
}