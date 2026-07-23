package com.kayque.compensa.history.service;

import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryCsvExportServiceTest {

    private final HistoryCsvExportService service =
            new HistoryCsvExportService(
                    ZoneId.of("America/Sao_Paulo")
            );

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldExportHistoryUsingReadableValues()
            throws Exception {
        PurchaseDecisionHistoryItem item =
                new PurchaseDecisionHistoryItem(
                        1,
                        "Mouse gamer",
                        new BigDecimal("159.90"),
                        154,
                        PurchaseDecisionStatus.THINK_AGAIN,
                        PurchaseDecisionOutcome.PURCHASED,
                        PurchaseSatisfaction.WORTH_IT,
                        Instant.parse(
                                "2026-07-22T23:30:00Z"
                        )
                );

        Path destination = temporaryDirectory.resolve(
                "historico.csv"
        );

        service.export(
                List.of(item),
                destination
        );

        String content = Files.readString(
                destination,
                StandardCharsets.UTF_8
        );

        assertTrue(content.contains("Compra"));
        assertTrue(content.contains("Mouse gamer"));
        assertTrue(content.contains("R$ 159,90"));
        assertTrue(content.contains("2h 34min"));
        assertTrue(content.contains("Pense novamente"));
        assertTrue(content.contains("Comprou"));
        assertTrue(content.contains("Valeu a pena"));
        assertTrue(content.contains("22/07/2026 20:30"));
    }

    @Test
    void shouldEscapeValueContainingSeparator()
            throws Exception {
        PurchaseDecisionHistoryItem item =
                new PurchaseDecisionHistoryItem(
                        2,
                        "Monitor; teclado",
                        new BigDecimal("500"),
                        60,
                        PurchaseDecisionStatus.MAKES_SENSE,
                        PurchaseDecisionOutcome.DECLINED,
                        null,
                        Instant.parse(
                                "2026-07-22T23:30:00Z"
                        )
                );

        Path destination = temporaryDirectory.resolve(
                "historico.csv"
        );

        service.export(
                List.of(item),
                destination
        );

        String content = Files.readString(
                destination,
                StandardCharsets.UTF_8
        );

        assertTrue(
                content.contains("\"Monitor; teclado\"")
        );
    }

    @Test
    void shouldRejectEmptyHistory() {
        Path destination = temporaryDirectory.resolve(
                "historico.csv"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.export(
                        List.of(),
                        destination
                )
        );
    }
}