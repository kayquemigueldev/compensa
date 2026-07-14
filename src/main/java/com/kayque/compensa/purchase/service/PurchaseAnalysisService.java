package com.kayque.compensa.purchase.service;

import com.kayque.compensa.profile.model.FinancialProfile;
import com.kayque.compensa.profile.service.WorkValueService;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PurchaseAnalysisService {

    private static final BigDecimal MINUTES_PER_HOUR =
            new BigDecimal("60");

    private final WorkValueService workValueService;

    public PurchaseAnalysisService() {
        this(new WorkValueService());
    }

    public PurchaseAnalysisService(
            WorkValueService workValueService
    ) {
        this.workValueService = Objects.requireNonNull(
                workValueService,
                "O serviço de valor da hora é obrigatório."
        );
    }

    public PurchaseAnalysis analyze(
            Purchase purchase,
            FinancialProfile profile
    ) {
        Objects.requireNonNull(
                purchase,
                "A compra é obrigatória."
        );

        Objects.requireNonNull(
                profile,
                "O perfil financeiro é obrigatório."
        );

        BigDecimal professionalHourlyValue =
                workValueService
                        .calculateProfessionalHourlyValue(profile);

        BigDecimal realHourlyValue =
                workValueService
                        .calculateRealHourlyValue(profile);

        long professionalWorkMinutes = calculateWorkMinutes(
                purchase.price(),
                professionalHourlyValue
        );

        long realWorkMinutes = calculateWorkMinutes(
                purchase.price(),
                realHourlyValue
        );

        BigDecimal projectedYearlyCost =
                calculateProjectedYearlyCost(purchase);

        return new PurchaseAnalysis(
                purchase,
                professionalWorkMinutes,
                realWorkMinutes,
                projectedYearlyCost
        );
    }

    private long calculateWorkMinutes(
            BigDecimal price,
            BigDecimal hourlyValue
    ) {
        return price
                .divide(hourlyValue, 8, RoundingMode.HALF_UP)
                .multiply(MINUTES_PER_HOUR)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private BigDecimal calculateProjectedYearlyCost(
            Purchase purchase
    ) {
        return purchase.price()
                .multiply(
                        BigDecimal.valueOf(
                                purchase.frequency()
                                        .yearlyOccurrences()
                        )
                )
                .setScale(2, RoundingMode.HALF_UP);
    }
}