package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.alerts.model.SmartAlertTopic;
import com.kayque.compensa.navigation.NavigationTarget;

import java.util.Objects;

public class DashboardAlertNavigationService {

    public NavigationTarget resolve(
            SmartAlertTopic topic
    ) {
        Objects.requireNonNull(
                topic,
                "O assunto do alerta é obrigatório."
        );

        return switch (topic) {
            case BUDGET_USAGE,
                 MONTHLY_SAVINGS_GOAL ->
                    NavigationTarget.FINANCIAL_PROFILE;

            case FINANCIAL_GOAL_PROGRESS ->
                    NavigationTarget.SAVINGS_GOAL;

            case PENDING_DECISIONS,
                 PURCHASE_EVALUATION,
                 WORK_TIME,
                 PRESERVED_VALUE ->
                    NavigationTarget.HISTORY;

            case PURCHASE_BEHAVIOR ->
                    NavigationTarget.INSIGHTS;
        };
    }
}