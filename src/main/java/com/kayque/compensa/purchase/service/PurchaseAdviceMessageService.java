package com.kayque.compensa.purchase.service;

import com.kayque.compensa.purchase.model.PurchaseAdviceMessage;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;
import com.kayque.compensa.userprofile.model.RecommendationTone;

import java.util.Objects;

public class PurchaseAdviceMessageService {

    public PurchaseAdviceMessage create(
            PurchaseDecisionStatus status,
            RecommendationTone tone
    ) {
        Objects.requireNonNull(
                status,
                "O status da análise é obrigatório."
        );

        Objects.requireNonNull(
                tone,
                "O tom da recomendação é obrigatório."
        );

        return switch (tone) {
            case GENTLE -> createGentleMessage(status);
            case BALANCED -> createBalancedMessage(status);
            case DIRECT -> createDirectMessage(status);
        };
    }

    private PurchaseAdviceMessage createGentleMessage(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> new PurchaseAdviceMessage(
                    "ESSA ESCOLHA PARECE FAZER SENTIDO",
                    "Pelos dados informados, esta compra parece compatível com seu momento. A decisão continua sendo sua."
            );

            case THINK_AGAIN -> new PurchaseAdviceMessage(
                    "QUE TAL REFLETIR MAIS UM POUCO?",
                    "A compra é possível, mas alguns pontos merecem sua atenção antes de decidir."
            );

            case PROBABLY_NOT_WORTH_IT ->
                    new PurchaseAdviceMessage(
                            "TALVEZ ESTE NÃO SEJA O MELHOR MOMENTO",
                            "Esta escolha apresenta impactos importantes. Considere esperar e rever suas prioridades com calma."
                    );
        };
    }

    private PurchaseAdviceMessage createBalancedMessage(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> new PurchaseAdviceMessage(
                    "PARECE FAZER SENTIDO",
                    "Os fatores informados são favoráveis à compra, sem eliminar a necessidade de uma decisão consciente."
            );

            case THINK_AGAIN -> new PurchaseAdviceMessage(
                    "PENSE MAIS UM POUCO",
                    "A compra é possível, mas existem pontos que merecem reflexão."
            );

            case PROBABLY_NOT_WORTH_IT ->
                    new PurchaseAdviceMessage(
                            "PROVAVELMENTE NÃO COMPENSA",
                            "O impacto desta compra é alto em relação ao contexto informado."
                    );
        };
    }

    private PurchaseAdviceMessage createDirectMessage(
            PurchaseDecisionStatus status
    ) {
        return switch (status) {
            case MAKES_SENSE -> new PurchaseAdviceMessage(
                    "A COMPRA FAZ SENTIDO",
                    "Os dados são favoráveis. Se a compra ainda for importante para você, ela cabe neste cenário."
            );

            case THINK_AGAIN -> new PurchaseAdviceMessage(
                    "NÃO DECIDA AGORA",
                    "Existem sinais de atenção. Espere e reavalie antes de gastar."
            );

            case PROBABLY_NOT_WORTH_IT ->
                    new PurchaseAdviceMessage(
                            "NÃO COMPENSA",
                            "O custo e o contexto tornam esta compra desfavorável neste momento."
                    );
        };
    }
}