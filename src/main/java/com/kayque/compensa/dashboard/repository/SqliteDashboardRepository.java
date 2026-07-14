package com.kayque.compensa.dashboard.repository;

import com.kayque.compensa.dashboard.model.DashboardSummary;
import com.kayque.compensa.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteDashboardRepository
        implements DashboardRepository {

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
                    new BigDecimal(
                            resultSet.getString(
                                    "preserved_value"
                            )
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
}