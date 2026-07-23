package com.kayque.compensa.alerts.service;

import com.kayque.compensa.alerts.model.SmartAlertDecisionMetrics;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.repository.SavingsGoalTimelineRepository;
import com.kayque.compensa.alerts.repository.SmartAlertMetricsRepository;
import com.kayque.compensa.goal.model.SavingsGoal;
import com.kayque.compensa.goal.model.SavingsGoalMonthlyPace;
import com.kayque.compensa.goal.model.SavingsGoalProgress;
import com.kayque.compensa.goal.model.SavingsGoalTargetPlan;
import com.kayque.compensa.goal.repository.SavingsGoalContributionRepository;
import com.kayque.compensa.goal.repository.SavingsGoalRepository;
import com.kayque.compensa.goal.service.SavingsGoalMonthlyPaceService;
import com.kayque.compensa.goal.service.SavingsGoalProgressService;
import com.kayque.compensa.goal.service.SavingsGoalTargetPlanService;
import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.model.MonthlyBudgetSummary;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;
import com.kayque.compensa.profile.repository.FinancialProfileRepository;
import com.kayque.compensa.profile.service.MonthlyBudgetService;
import com.kayque.compensa.profile.service.MonthlyBudgetUsageService;
import com.kayque.compensa.purchase.service.CurrentMonthPurchasedAmountService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public class SmartAlertSnapshotProvider
        implements SmartAlertSnapshotSource {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final long OVERDUE_DECISION_DAYS = 7;

    private final FinancialProfileRepository
            financialProfileRepository;

    private final MonthlyBudgetService monthlyBudgetService;
    private final MonthlyBudgetUsageService budgetUsageService;

    private final CurrentMonthPurchasedAmountService
            purchasedAmountService;

    private final SavingsGoalRepository savingsGoalRepository;

    private final SavingsGoalContributionRepository
            contributionRepository;

    private final SavingsGoalProgressService goalProgressService;
    private final SavingsGoalTargetPlanService targetPlanService;
    private final SavingsGoalMonthlyPaceService monthlyPaceService;

    private final SmartAlertMetricsRepository metricsRepository;

    private final SavingsGoalTimelineRepository
            goalTimelineRepository;

    private final GoalExpectedProgressService
            expectedProgressService;

    private final Clock clock;

    public SmartAlertSnapshotProvider(
            FinancialProfileRepository financialProfileRepository,
            MonthlyBudgetService monthlyBudgetService,
            MonthlyBudgetUsageService budgetUsageService,
            CurrentMonthPurchasedAmountService purchasedAmountService,
            SavingsGoalRepository savingsGoalRepository,
            SavingsGoalContributionRepository contributionRepository,
            SavingsGoalProgressService goalProgressService,
            SavingsGoalTargetPlanService targetPlanService,
            SavingsGoalMonthlyPaceService monthlyPaceService,
            SmartAlertMetricsRepository metricsRepository,
            SavingsGoalTimelineRepository goalTimelineRepository,
            GoalExpectedProgressService expectedProgressService,
            Clock clock
    ) {
        this.financialProfileRepository =
                Objects.requireNonNull(
                        financialProfileRepository
                );

        this.monthlyBudgetService =
                Objects.requireNonNull(monthlyBudgetService);

        this.budgetUsageService =
                Objects.requireNonNull(budgetUsageService);

        this.purchasedAmountService =
                Objects.requireNonNull(purchasedAmountService);

        this.savingsGoalRepository =
                Objects.requireNonNull(savingsGoalRepository);

        this.contributionRepository =
                Objects.requireNonNull(contributionRepository);

        this.goalProgressService =
                Objects.requireNonNull(goalProgressService);

        this.targetPlanService =
                Objects.requireNonNull(targetPlanService);

        this.monthlyPaceService =
                Objects.requireNonNull(monthlyPaceService);

        this.metricsRepository =
                Objects.requireNonNull(metricsRepository);

        this.goalTimelineRepository =
                Objects.requireNonNull(goalTimelineRepository);

        this.expectedProgressService =
                Objects.requireNonNull(expectedProgressService);

        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public SmartAlertSnapshot createSnapshot() {
        LocalDate currentDate = LocalDate.now(clock);

        SmartAlertDecisionMetrics decisionMetrics =
                loadDecisionMetrics(currentDate);

        BudgetValues budgetValues = loadBudgetValues();
        GoalValues goalValues = loadGoalValues(currentDate);

        return new SmartAlertSnapshot(
                budgetValues.usagePercentage(),
                budgetValues.availableAmount(),
                goalValues.monthlyContributions(),
                goalValues.monthlyTarget(),
                goalValues.currentProgress(),
                goalValues.expectedProgress(),
                decisionMetrics.purchasesMadeThisMonth(),
                decisionMetrics.purchasesAvoidedThisMonth(),
                decisionMetrics.pendingDecisions(),
                decisionMetrics.overduePendingDecisions(),
                decisionMetrics.workMinutesThisMonth(),
                decisionMetrics.preservedAmountThisYear()
        );
    }

    private SmartAlertDecisionMetrics loadDecisionMetrics(
            LocalDate currentDate
    ) {
        ZoneId zone = clock.getZone();

        YearMonth currentMonth =
                YearMonth.from(currentDate);

        LocalDate monthStart =
                currentMonth.atDay(1);

        LocalDate nextMonthStart =
                currentMonth.plusMonths(1).atDay(1);

        LocalDate yearStart =
                currentDate.withDayOfYear(1);

        LocalDate nextYearStart =
                yearStart.plusYears(1);

        Instant overdueCutoff =
                clock.instant().minus(
                        OVERDUE_DECISION_DAYS,
                        ChronoUnit.DAYS
                );

        return metricsRepository.getDecisionMetrics(
                monthStart.atStartOfDay(zone).toInstant(),
                nextMonthStart.atStartOfDay(zone).toInstant(),
                yearStart.atStartOfDay(zone).toInstant(),
                nextYearStart.atStartOfDay(zone).toInstant(),
                overdueCutoff
        );
    }

    private BudgetValues loadBudgetValues() {
        FinancialProfile profile =
                financialProfileRepository
                        .find()
                        .orElse(null);

        if (profile == null) {
            return BudgetValues.empty();
        }

        MonthlyBudgetSummary plannedBudget =
                monthlyBudgetService.calculate(profile);

        BigDecimal purchasedAmount =
                purchasedAmountService.calculate();

        MonthlyBudgetUsage usage =
                budgetUsageService.calculate(
                        plannedBudget,
                        purchasedAmount
                );

        BigDecimal percentage =
                calculateBudgetUsagePercentage(usage);

        return new BudgetValues(
                percentage,
                usage.currentAvailableAmount()
        );
    }

    private BigDecimal calculateBudgetUsagePercentage(
            MonthlyBudgetUsage usage
    ) {
        BigDecimal plannedAmount =
                usage.plannedAvailableAmount();

        if (plannedAmount.signum() <= 0) {
            return usage.purchasedAmount().signum() > 0
                    ? ONE_HUNDRED
                    : BigDecimal.ZERO;
        }

        return usage.purchasedAmount()
                .multiply(ONE_HUNDRED)
                .divide(
                        plannedAmount,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private GoalValues loadGoalValues(
            LocalDate currentDate
    ) {
        SavingsGoal goal =
                savingsGoalRepository
                        .find()
                        .orElse(null);

        if (goal == null) {
            return GoalValues.empty();
        }

        SavingsGoalProgress progress =
                goalProgressService.calculate(goal);

        SavingsGoalTargetPlan targetPlan =
                targetPlanService.calculate(
                        goal,
                        currentDate
                );

        SavingsGoalMonthlyPace monthlyPace =
                monthlyPaceService.calculate(
                        targetPlan,
                        contributionRepository.findAll(),
                        currentDate
                );

        BigDecimal expectedProgress =
                calculateExpectedProgress(
                        goal,
                        progress,
                        currentDate
                );

        return new GoalValues(
                monthlyPace.contributedThisMonth(),
                monthlyPace.requiredMonthlyAmount(),
                progress.percentage(),
                expectedProgress
        );
    }

    private BigDecimal calculateExpectedProgress(
            SavingsGoal goal,
            SavingsGoalProgress progress,
            LocalDate currentDate
    ) {
        if (!goal.hasTargetDate()) {
            return progress.percentage();
        }

        return goalTimelineRepository
                .findCreatedDate()
                .map(createdDate ->
                        expectedProgressService.calculate(
                                createdDate,
                                goal.targetDate(),
                                currentDate
                        )
                )
                .orElse(progress.percentage());
    }

    private record BudgetValues(
            BigDecimal usagePercentage,
            BigDecimal availableAmount
    ) {

        private static BudgetValues empty() {
            return new BudgetValues(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
    }

    private record GoalValues(
            BigDecimal monthlyContributions,
            BigDecimal monthlyTarget,
            BigDecimal currentProgress,
            BigDecimal expectedProgress
    ) {

        private static GoalValues empty() {
            return new GoalValues(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
    }
}