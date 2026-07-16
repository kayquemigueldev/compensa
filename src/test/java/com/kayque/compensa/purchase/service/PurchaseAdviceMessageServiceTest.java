package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.model.PurchaseAdviceMessage;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.userprofile.model.RecommendationTone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseAdviceMessageServiceTest {

    private final PurchaseAdviceMessageService service =
            new PurchaseAdviceMessageService();

    @Test
    void shouldCreateGentleMessage() {
        PurchaseAdviceMessage message = service.create(
                PurchaseDecisionStatus.THINK_AGAIN,
                RecommendationTone.GENTLE
        );

        assertEquals(
                "QUE TAL REFLETIR MAIS UM POUCO?",
                message.title()
        );
    }

    @Test
    void shouldCreateBalancedMessage() {
        PurchaseAdviceMessage message = service.create(
                PurchaseDecisionStatus.THINK_AGAIN,
                RecommendationTone.BALANCED
        );

        assertEquals(
                "PENSE MAIS UM POUCO",
                message.title()
        );
    }

    @Test
    void shouldCreateDirectMessage() {
        PurchaseAdviceMessage message = service.create(
                PurchaseDecisionStatus.THINK_AGAIN,
                RecommendationTone.DIRECT
        );

        assertEquals(
                "NÃO DECIDA AGORA",
                message.title()
        );
    }
}