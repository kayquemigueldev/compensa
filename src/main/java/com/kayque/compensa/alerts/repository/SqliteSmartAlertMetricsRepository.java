package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.alerts.model.SmartAlertDecisionMetrics;
import com.kayque.compensa.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SqliteSmartAlertMetricsRepository
        implements SmartAlertMetricsRepository {

    private static final DateTimeFormatter DATABASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    private static final String FIND_METRICS = """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'PURCHASED'
                            AND created_at >= ?
                            AND created_at < ?
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS purchases_made_this_month,

            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'DECLINED'
                            AND created_at >= ?
                            AND created_at < ?
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS purchases_avoided_this_month,

            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'WAITING'
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS pending_decisions,

            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'WAITING'
                            AND created_at < ?
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS overdue_pending_decisions,

            COALESCE(
                SUM(
                    CASE
                        WHEN created_at >= ?
                            AND created_at < ?
                        THEN real_work_minutes
                        ELSE 0
                    END
                ),
                0
            ) AS work_minutes_this_month,

            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'DECLINED'
                            AND created_at >= ?
                            AND created_at < ?
                        THEN price
                        ELSE 0
                    END
                ),
                0
            ) AS preserved_amount_this_year

        FROM purchase_decision
        """;

    @Override
    public SmartAlertDecisionMetrics getDecisionMetrics(
            Instant monthStartInclusive,
            Instant nextMonthStartExclusive,
            Instant yearStartInclusive,
            Instant nextYearStartExclusive,
            Instant overdueCutoffExclusive
    ) {
        validatePeriod(
                monthStartInclusive,
                nextMonthStartExclusive,
                "O período mensal é inválido."
        );

        validatePeriod(
                yearStartInclusive,
                nextYearStartExclusive,
                "O período anual é inválido."
        );

        Objects.requireNonNull(
                overdueCutoffExclusive,
                "O limite das decisões atrasadas é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_METRICS)
        ) {
            statement.setString(
                    1,
                    formatInstant(monthStartInclusive)
            );

            statement.setString(
                    2,
                    formatInstant(nextMonthStartExclusive)
            );

            statement.setString(
                    3,
                    formatInstant(monthStartInclusive)
            );

            statement.setString(
                    4,
                    formatInstant(nextMonthStartExclusive)
            );

            statement.setString(
                    5,
                    formatInstant(overdueCutoffExclusive)
            );

            statement.setString(
                    6,
                    formatInstant(monthStartInclusive)
            );

            statement.setString(
                    7,
                    formatInstant(nextMonthStartExclusive)
            );

            statement.setString(
                    8,
                    formatInstant(yearStartInclusive)
            );

            statement.setString(
                    9,
                    formatInstant(nextYearStartExclusive)
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return emptyMetrics();
                }

                BigDecimal preservedAmount =
                        resultSet.getBigDecimal(
                                "preserved_amount_this_year"
                        );

                if (preservedAmount == null) {
                    preservedAmount = BigDecimal.ZERO;
                }

                return new SmartAlertDecisionMetrics(
                        resultSet.getInt(
                                "purchases_made_this_month"
                        ),

                        resultSet.getInt(
                                "purchases_avoided_this_month"
                        ),

                        resultSet.getInt(
                                "pending_decisions"
                        ),

                        resultSet.getInt(
                                "overdue_pending_decisions"
                        ),

                        resultSet.getLong(
                                "work_minutes_this_month"
                        ),

                        preservedAmount
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar as métricas dos alertas.",
                    exception
            );
        }
    }

    private void validatePeriod(
            Instant start,
            Instant end,
            String message
    ) {
        Objects.requireNonNull(
                start,
                "O início do período é obrigatório."
        );

        Objects.requireNonNull(
                end,
                "O fim do período é obrigatório."
        );

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String formatInstant(Instant instant) {
        return LocalDateTime
                .ofInstant(
                        instant,
                        ZoneOffset.UTC
                )
                .format(DATABASE_DATE_FORMAT);
    }

    private SmartAlertDecisionMetrics emptyMetrics() {
        return new SmartAlertDecisionMetrics(
                0,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO
        );
    }
}