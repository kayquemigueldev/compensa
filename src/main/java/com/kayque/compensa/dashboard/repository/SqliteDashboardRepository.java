package com.kayque.compensa.dashboard.repository;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.dashboard.model.DashboardWeeklySummary;
import com.kayque.compensa.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SqliteDashboardRepository
        implements DashboardRepository {

    private static final DateTimeFormatter
            DATABASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            ).withZone(ZoneOffset.UTC);

    private static final String SUMMARY_QUERY = """
            SELECT
                COUNT(*) AS total_decisions,

                SUM(
                    CASE
                        WHEN outcome = 'PURCHASED' THEN 1
                        ELSE 0
                    END
                ) AS purchased_decisions,

                SUM(
                    CASE
                        WHEN outcome = 'DECLINED' THEN 1
                        ELSE 0
                    END
                ) AS declined_decisions,

                SUM(
                    CASE
                        WHEN outcome = 'WAITING' THEN 1
                        ELSE 0
                    END
                ) AS waiting_decisions,

                COALESCE(
                    SUM(
                        CASE
                            WHEN outcome = 'DECLINED'
                            THEN price
                            ELSE 0
                        END
                    ),
                    0
                ) AS preserved_value,

                COALESCE(
                    SUM(real_work_minutes),
                    0
                ) AS total_real_work_minutes

            FROM purchase_decision
            """;

    private static final String WEEKLY_SUMMARY_QUERY = """
            SELECT
                COUNT(*) AS total_decisions,

                SUM(
                    CASE
                        WHEN outcome = 'PURCHASED' THEN 1
                        ELSE 0
                    END
                ) AS purchased_decisions,

                SUM(
                    CASE
                        WHEN outcome = 'DECLINED' THEN 1
                        ELSE 0
                    END
                ) AS declined_decisions,

                COALESCE(
                    SUM(
                        CASE
                            WHEN outcome = 'PURCHASED'
                            THEN price
                            ELSE 0
                        END
                    ),
                    0
                ) AS purchased_value,

                COALESCE(
                    SUM(
                        CASE
                            WHEN outcome = 'DECLINED'
                            THEN price
                            ELSE 0
                        END
                    ),
                    0
                ) AS preserved_value,

                COALESCE(
                    SUM(real_work_minutes),
                    0
                ) AS total_real_work_minutes

            FROM purchase_decision
            WHERE created_at >= ?
              AND created_at < ?
            """;

    @Override
    public DashboardSummary getSummary() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                SUMMARY_QUERY
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            if (!resultSet.next()) {
                return emptySummary();
            }

            return new DashboardSummary(
                    resultSet.getLong("total_decisions"),
                    resultSet.getLong("purchased_decisions"),
                    resultSet.getLong("declined_decisions"),
                    resultSet.getLong("waiting_decisions"),

                    readMoney(
                            resultSet,
                            "preserved_value"
                    ),

                    resultSet.getLong(
                            "total_real_work_minutes"
                    )
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar os indicadores.",
                    exception
            );
        }
    }

    @Override
    public DashboardWeeklySummary getWeeklySummary(
            Instant startInclusive,
            Instant endExclusive
    ) {
        validatePeriod(
                startInclusive,
                endExclusive
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                WEEKLY_SUMMARY_QUERY
                        )
        ) {
            statement.setString(
                    1,
                    DATABASE_DATE_FORMAT.format(
                            startInclusive
                    )
            );

            statement.setString(
                    2,
                    DATABASE_DATE_FORMAT.format(
                            endExclusive
                    )
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {
                if (!resultSet.next()) {
                    return emptyWeeklySummary();
                }

                return new DashboardWeeklySummary(
                        resultSet.getLong(
                                "total_decisions"
                        ),

                        resultSet.getLong(
                                "purchased_decisions"
                        ),

                        resultSet.getLong(
                                "declined_decisions"
                        ),

                        readMoney(
                                resultSet,
                                "purchased_value"
                        ),

                        readMoney(
                                resultSet,
                                "preserved_value"
                        ),

                        resultSet.getLong(
                                "total_real_work_minutes"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o resumo semanal.",
                    exception
            );
        }
    }

    private BigDecimal readMoney(
            ResultSet resultSet,
            String column
    ) throws SQLException {
        String value = resultSet.getString(column);

        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value);
    }

    private void validatePeriod(
            Instant startInclusive,
            Instant endExclusive
    ) {
        Objects.requireNonNull(
                startInclusive,
                "O início do período é obrigatório."
        );

        Objects.requireNonNull(
                endExclusive,
                "O fim do período é obrigatório."
        );

        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException(
                    "O início do período deve ser anterior ao fim."
            );
        }
    }

    private DashboardSummary emptySummary() {
        return new DashboardSummary(
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                0
        );
    }

    private DashboardWeeklySummary
    emptyWeeklySummary() {
        return new DashboardWeeklySummary(
                0,
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
    }
}