package com.kayque.compensa.insights.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.insights.model.InsightsSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteInsightsRepository
        implements InsightsRepository {

    private static final String INSIGHTS_QUERY = """
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

                SUM(
                    CASE
                        WHEN outcome = 'PURCHASED'
                         AND advice_status IN (
                            'THINK_AGAIN',
                            'PROBABLY_NOT_WORTH_IT'
                         )
                        THEN 1
                        ELSE 0
                    END
                ) AS purchases_against_advice,

                COALESCE(
                    SUM(real_work_minutes),
                    0
                ) AS total_real_work_minutes

            FROM purchase_decision
            """;

    @Override
    public InsightsSummary getSummary() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                INSIGHTS_QUERY
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            if (!resultSet.next()) {
                return emptySummary();
            }

            return new InsightsSummary(
                    resultSet.getLong("total_decisions"),
                    resultSet.getLong("purchased_decisions"),
                    resultSet.getLong("declined_decisions"),
                    resultSet.getLong("waiting_decisions"),
                    resultSet.getLong(
                            "purchases_against_advice"
                    ),
                    resultSet.getLong(
                            "total_real_work_minutes"
                    )
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar os insights.",
                    exception
            );
        }
    }

    private InsightsSummary emptySummary() {
        return new InsightsSummary(
                0,
                0,
                0,
                0,
                0,
                0
        );
    }
}