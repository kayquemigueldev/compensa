package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.FinancialProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class WorkValueService {

    private static final int MONEY_SCALE = 2;

    public BigDecimal calculateProfessionalHourlyValue(
            FinancialProfile profile
    ) {
        Objects.requireNonNull(
                profile,
                "O perfil financeiro não pode ser nulo."
        );

        return divideIncomeByHours(
                profile.netMonthlyIncome(),
                profile.monthlyWorkHours()
        );
    }

    public BigDecimal calculateRealHourlyValue(
            FinancialProfile profile
    ) {
        Objects.requireNonNull(
                profile,
                "O perfil financeiro não pode ser nulo."
        );

        return divideIncomeByHours(
                profile.netMonthlyIncome(),
                profile.totalCommittedHours()
        );
    }

    private BigDecimal divideIncomeByHours(
            BigDecimal income,
            BigDecimal hours
    ) {
        return income.divide(
                hours,
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }
}