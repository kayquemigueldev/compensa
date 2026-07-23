package com.kayque.compensa.alerts.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class GoalExpectedProgressService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    public BigDecimal calculate(
            LocalDate createdDate,
            LocalDate targetDate,
            LocalDate currentDate
    ) {
        Objects.requireNonNull(
                createdDate,
                "A data de criação do objetivo é obrigatória."
        );

        Objects.requireNonNull(
                targetDate,
                "A data final do objetivo é obrigatória."
        );

        Objects.requireNonNull(
                currentDate,
                "A data atual é obrigatória."
        );

        if (targetDate.isBefore(createdDate)) {
            throw new IllegalArgumentException(
                    "A data final não pode ser anterior à criação do objetivo."
            );
        }

        if (currentDate.isBefore(createdDate)) {
            return BigDecimal.ZERO;
        }

        if (!currentDate.isBefore(targetDate)) {
            return ONE_HUNDRED;
        }

        long totalDays = ChronoUnit.DAYS.between(
                createdDate,
                targetDate
        );

        if (totalDays == 0) {
            return ONE_HUNDRED;
        }

        long elapsedDays = ChronoUnit.DAYS.between(
                createdDate,
                currentDate
        );

        return BigDecimal.valueOf(elapsedDays)
                .multiply(ONE_HUNDRED)
                .divide(
                        BigDecimal.valueOf(totalDays),
                        2,
                        RoundingMode.HALF_UP
                )
                .max(BigDecimal.ZERO)
                .min(ONE_HUNDRED);
    }
}