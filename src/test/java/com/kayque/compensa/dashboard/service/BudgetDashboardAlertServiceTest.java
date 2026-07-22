package com.kayque.compensa.dashboard.service;

import com.kayque.compensa.dashboard.model.DashboardAlert;
import com.kayque.compensa.dashboard.model.DashboardAlertLevel;
import com.kayque.compensa.profile.model.MonthlyBudgetStatus;
import com.kayque.compensa.profile.model.MonthlyBudgetUsage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetDashboardAlertServiceTest {

    private final BudgetDashboardAlertService service =
            new BudgetDashboardAlertService();

    @Test
    void shouldNotCreateAlertBelowSeventyPercent() {
        Optional<DashboardAlert> alert = service.create(
                createUsage(
                        "1000",
                        "699.90",
                        MonthlyBudgetStatus.AVAILABLE
                )
        );

        assertTrue(alert.isEmpty());
    }

    @Test
    void shouldCreateInformationAlertAtSeventyPercent() {
        DashboardAlert alert = service.create(
                createUsage(
                        "1000",
                        "700",
                        MonthlyBudgetStatus.AVAILABLE
                )
        ).orElseThrow();

        assertEquals(
                "Orçamento entrando em atenção",
                alert.title()
        );

        assertEquals(
                DashboardAlertLevel.INFORMATION,
                alert.level()
        );
    }

    @Test
    void shouldKeepInformationAlertBelowNinetyPercent() {
        DashboardAlert alert = service.create(
                createUsage(
                        "1000",
                        "899.90",
                        MonthlyBudgetStatus.AVAILABLE
                )
        ).orElseThrow();

        assertEquals(
                DashboardAlertLevel.INFORMATION,
                alert.level()
        );
    }

    @Test
    void shouldCreateAttentionAlertAtNinetyPercent() {
        DashboardAlert alert = service.create(
                createUsage(
                        "1000",
                        "900",
                        MonthlyBudgetStatus.AVAILABLE
                )
        ).orElseThrow();

        assertEquals(
                "Pouca margem disponível",
                alert.title()
        );

        assertEquals(
                DashboardAlertLevel.ATTENTION,
                alert.level()
        );
    }

    @Test
    void shouldCreateLimitAlertAtOneHundredPercent() {
        DashboardAlert alert = service.create(
                createUsage(
                        "1000",
                        "1000",
                        MonthlyBudgetStatus.BALANCED
                )
        ).orElseThrow();

        assertEquals(
                "Orçamento mensal no limite",
                alert.title()
        );

        assertEquals(
                DashboardAlertLevel.ATTENTION,
                alert.level()
        );
    }

    @Test
    void shouldCreateExceededAlertForDeficit() {
        DashboardAlert alert = service.create(
                createUsage(
                        "1000",
                        "1200",
                        MonthlyBudgetStatus.DEFICIT
                )
        ).orElseThrow();

        assertEquals(
                "Orçamento mensal ultrapassado",
                alert.title()
        );

        assertEquals(
                DashboardAlertLevel.ATTENTION,
                alert.level()
        );
    }

    private MonthlyBudgetUsage createUsage(
            String plannedAmount,
            String purchasedAmount,
            MonthlyBudgetStatus status
    ) {
        BigDecimal planned =
                new BigDecimal(plannedAmount);

        BigDecimal purchased =
                new BigDecimal(purchasedAmount);

        return new MonthlyBudgetUsage(
                planned,
                purchased,
                planned.subtract(purchased),
                status
        );
    }
}