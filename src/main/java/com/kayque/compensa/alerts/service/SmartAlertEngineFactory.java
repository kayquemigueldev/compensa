package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.rule.BudgetUsageAlertRule;
import com.kayque.compensa.alerts.rule.FinancialGoalProgressAlertRule;
import com.kayque.compensa.alerts.rule.MonthlySavingsGoalAlertRule;
import com.kayque.compensa.alerts.rule.PendingDecisionAlertRule;
import com.kayque.compensa.alerts.rule.PreservedValueAlertRule;
import com.kayque.compensa.alerts.rule.PurchaseBehaviorAlertRule;
import com.kayque.compensa.alerts.rule.SmartAlertRule;
import com.kayque.compensa.alerts.rule.WorkTimeAlertRule;

import java.util.List;

public final class SmartAlertEngineFactory {

    private SmartAlertEngineFactory() {
    }

    public static SmartAlertEngine createDefault() {
        List<SmartAlertRule> rules = List.of(
                new BudgetUsageAlertRule(),
                new MonthlySavingsGoalAlertRule(),
                new FinancialGoalProgressAlertRule(),
                new PurchaseBehaviorAlertRule(),
                new PendingDecisionAlertRule(),
                new WorkTimeAlertRule(),
                new PreservedValueAlertRule()
        );

        return new SmartAlertEngine(rules);
    }
}