package com.kayque.compensa.profile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyWorkHoursCalculation(
        BigDecimal monthlyProfessionalHours,
        BigDecimal monthlyAdditionalHours,
        BigDecimal monthlyCommittedHours
) {

    public MonthlyWorkHoursCalculation {
        Objects.requireNonNull(
                monthlyProfessionalHours,
                "As horas profissionais são obrigatórias."
        );

        Objects.requireNonNull(
                monthlyAdditionalHours,
                "As horas adicionais são obrigatórias."
        );

        Objects.requireNonNull(
                monthlyCommittedHours,
                "O total de horas comprometidas é obrigatório."
        );

        if (monthlyProfessionalHours.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new IllegalArgumentException(
                    "As horas profissionais devem ser maiores que zero."
            );
        }

        if (monthlyAdditionalHours.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new IllegalArgumentException(
                    "As horas adicionais não podem ser negativas."
            );
        }

        if (monthlyCommittedHours.compareTo(
                monthlyProfessionalHours
        ) < 0) {
            throw new IllegalArgumentException(
                    "O total comprometido não pode ser menor que a jornada profissional."
            );
        }
    }
}