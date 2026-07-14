package com.kayque.compensa.purchase.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseFrequency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseAnalysisServiceTest {

    private final PurchaseAnalysisService service =
            new PurchaseAnalysisService();

    @Test
    void shouldAnalyzeWeeklyPurchaseImpact() {
        FinancialProfile profile = new FinancialProfile(
                new BigDecimal("2000.00"),
                new BigDecimal("160"),
                new BigDecimal("40")
        );

        Purchase purchase = new Purchase(
                "Salgadinho",
                new BigDecimal("7.99"),
                PurchaseFrequency.WEEKLY
        );

        PurchaseAnalysis analysis =
                service.analyze(purchase, profile);

        assertEquals(
                38,
                analysis.professionalWorkMinutes()
        );

        assertEquals(
                48,
                analysis.realWorkMinutes()
        );

        assertEquals(
                new BigDecimal("415.48"),
                analysis.projectedYearlyCost()
        );
    }
}