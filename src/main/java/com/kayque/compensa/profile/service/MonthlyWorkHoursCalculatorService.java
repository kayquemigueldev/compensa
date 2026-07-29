package com.kayque.compensa.profile.service;

import com.kayque.compensa.profile.model.MonthlyWorkHoursCalculation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlyWorkHoursCalculatorService {

    private static final BigDecimal MONTHS_PER_YEAR =
            new BigDecimal("12");

    private static final BigDecimal WEEKS_PER_YEAR =
            new BigDecimal("52");

    private static final BigDecimal MAXIMUM_DAILY_HOURS =
            new BigDecimal("24");

    private static final int RESULT_SCALE = 2;
    private static final int CALCULATION_SCALE = 10;

    public MonthlyWorkHoursCalculation calculate(
            BigDecimal professionalHoursPerDay,
            int workDaysPerWeek,
            BigDecimal additionalHoursPerDay
    ) {
        validate(
                professionalHoursPerDay,
                workDaysPerWeek,
                additionalHoursPerDay
        );

        BigDecimal averageWeeksPerMonth =
                WEEKS_PER_YEAR.divide(
                        MONTHS_PER_YEAR,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal workDays =
                BigDecimal.valueOf(workDaysPerWeek);

        BigDecimal monthlyProfessionalHours =
                professionalHoursPerDay
                        .multiply(workDays)
                        .multiply(averageWeeksPerMonth)
                        .setScale(
                                RESULT_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal monthlyAdditionalHours =
                additionalHoursPerDay
                        .multiply(workDays)
                        .multiply(averageWeeksPerMonth)
                        .setScale(
                                RESULT_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal monthlyCommittedHours =
                monthlyProfessionalHours
                        .add(monthlyAdditionalHours)
                        .setScale(
                                RESULT_SCALE,
                                RoundingMode.HALF_UP
                        );

        return new MonthlyWorkHoursCalculation(
                monthlyProfessionalHours,
                monthlyAdditionalHours,
                monthlyCommittedHours
        );
    }

    private void validate(
            BigDecimal professionalHoursPerDay,
            int workDaysPerWeek,
            BigDecimal additionalHoursPerDay
    ) {
        if (professionalHoursPerDay == null) {
            throw new IllegalArgumentException(
                    "Informe as horas trabalhadas por dia."
            );
        }

        if (professionalHoursPerDay.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new IllegalArgumentException(
                    "As horas trabalhadas por dia devem ser maiores que zero."
            );
        }

        if (workDaysPerWeek < 1 || workDaysPerWeek > 7) {
            throw new IllegalArgumentException(
                    "Os dias trabalhados por semana devem estar entre 1 e 7."
            );
        }

        if (additionalHoursPerDay == null) {
            throw new IllegalArgumentException(
                    "Informe o tempo adicional diário ou utilize zero."
            );
        }

        if (additionalHoursPerDay.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "O tempo adicional diário não pode ser negativo."
            );
        }

        BigDecimal totalDailyHours =
                professionalHoursPerDay.add(
                        additionalHoursPerDay
                );

        if (totalDailyHours.compareTo(
                MAXIMUM_DAILY_HOURS
        ) > 0) {
            throw new IllegalArgumentException(
                    "O tempo total comprometido por dia não pode ultrapassar 24 horas."
            );
        }
    }
}