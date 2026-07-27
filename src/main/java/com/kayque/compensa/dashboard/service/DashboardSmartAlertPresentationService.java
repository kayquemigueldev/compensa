package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.dashboard.model.DashboardSmartAlertView;
import com.kayque.compensa.navigation.NavigationTarget;

import java.util.List;
import java.util.Objects;

public class DashboardSmartAlertPresentationService {

    private final DashboardAlertNavigationService
            navigationService;

    public DashboardSmartAlertPresentationService() {
        this(new DashboardAlertNavigationService());
    }

    public DashboardSmartAlertPresentationService(
            DashboardAlertNavigationService navigationService
    ) {
        this.navigationService = Objects.requireNonNull(
                navigationService,
                "O serviço de navegação é obrigatório."
        );
    }

    public List<DashboardSmartAlertView> prepare(
            List<SmartAlert> alerts,
            int limit
    ) {
        validateAlerts(alerts);

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "O limite de alertas deve ser positivo."
            );
        }

        return alerts.stream()
                .limit(limit)
                .map(this::createView)
                .toList();
    }

    public List<DashboardSmartAlertView> prepareAll(
            List<SmartAlert> alerts
    ) {
        validateAlerts(alerts);

        return alerts.stream()
                .map(this::createView)
                .toList();
    }

    private void validateAlerts(
            List<SmartAlert> alerts
    ) {
        Objects.requireNonNull(
                alerts,
                "A lista de alertas é obrigatória."
        );

        if (alerts.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "A lista de alertas não pode conter valores nulos."
            );
        }
    }

    private DashboardSmartAlertView createView(
            SmartAlert alert
    ) {
        NavigationTarget navigationTarget =
                navigationService.resolve(
                        alert.topic()
                );

        return new DashboardSmartAlertView(
                alert.code(),
                alert.title(),
                alert.message(),
                getExplanation(alert.topic()),
                getStyleClass(alert.priority()),
                navigationTarget
        );
    }

    private String getExplanation(
            SmartAlertTopic topic
    ) {
        return switch (topic) {
            case BUDGET_USAGE ->
                    "Este alerta compara o total comprado neste mês com o dinheiro livre planejado no seu perfil financeiro.";

            case MONTHLY_SAVINGS_GOAL ->
                    "Este alerta compara quanto você definiu como meta de economia com o valor disponível no orçamento mensal.";

            case FINANCIAL_GOAL_PROGRESS ->
                    "Este alerta compara o progresso atual do seu objetivo com o avanço esperado para este momento.";

            case PENDING_DECISIONS ->
                    "Este alerta considera as compras que continuam aguardando uma decisão no seu histórico.";

            case PURCHASE_EVALUATION ->
                    "Este alerta considera compras realizadas que ainda não receberam uma avaliação de satisfação.";

            case WORK_TIME ->
                    "Este alerta soma o tempo real de trabalho necessário para pagar as compras realizadas neste mês.";

            case PRESERVED_VALUE ->
                    "Este alerta considera o valor das compras que você decidiu não realizar durante o período analisado.";

            case PURCHASE_BEHAVIOR ->
                    "Este alerta observa padrões recorrentes nas suas decisões, como impulso, planejamento e satisfação.";
        };
    }

    private String getStyleClass(
            SmartAlertPriority priority
    ) {
        return switch (priority) {
            case INFORMATIONAL ->
                    "dashboard-smart-alert-informational";

            case ATTENTION ->
                    "dashboard-smart-alert-attention";

            case CRITICAL ->
                    "dashboard-smart-alert-critical";
        };
    }
}