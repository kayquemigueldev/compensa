package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.repository.SqliteSavingsGoalTimelineRepository;
import com.kayque.compensa.alerts.repository.SqliteSmartAlertMetricsRepository;
import com.kayque.compensa.goal.repository.SavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SqliteSavingsGoalRepository;
import com.kayque.compensa.goal.service.SavingsGoalMonthlyPaceService;
import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import com.kayque.compensa.goal.service.SavingsGoalTargetPlanService;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.repository.SqliteFinancialProfileRepository;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
import com.kayque.compensa.profile.service.MonthlyBudgetUsageService;
import com.kayque.compensa.purchase.repository.PurchaseDecisionRepository;
import com.kayque.compensa.purchase.repository.SqlitePurchaseDecisionRepository;
import com.kayque.compensa.purchase.service.CurrentMonthPurchasedAmountService;

import java.time.Clock;

public final class SmartAlertSnapshotProviderFactory {

    private SmartAlertSnapshotProviderFactory() {
    }

    public static SmartAlertSnapshotProvider createDefault() {
        FinancialProfileRepository financialProfileRepository =
                new SqliteFinancialProfileRepository();

        PurchaseDecisionRepository purchaseDecisionRepository =
                new SqlitePurchaseDecisionRepository();

        SavingsGoalRepository savingsGoalRepository =
                new SqliteSavingsGoalRepository();

        SavingsGoalContributionRepository contributionRepository =
                new SqliteSavingsGoalContributionRepository();

        CurrentMonthPurchasedAmountService purchasedAmountService =
                new CurrentMonthPurchasedAmountService(
                        purchaseDecisionRepository
                );

        return new SmartAlertSnapshotProvider(
                financialProfileRepository,
                new MonthlyBudgetService(),
                new MonthlyBudgetUsageService(),
                purchasedAmountService,
                savingsGoalRepository,
                contributionRepository,
                new SavingsGoalProgressService(),
                new SavingsGoalTargetPlanService(),
                new SavingsGoalMonthlyPaceService(),
                new SqliteSmartAlertMetricsRepository(),
                new SqliteSavingsGoalTimelineRepository(),
                new GoalExpectedProgressService(),
                Clock.systemDefaultZone()
        );
    }
}