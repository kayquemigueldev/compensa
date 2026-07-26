package com.kayque.compensa.dashboard.repository;

import com.kayque.compensa.dashboard.model.PurchaseAttentionSummary;
import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlitePurchaseAttentionRepository
        implements PurchaseAttentionRepository {

    private static final String SUMMARIZE = """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'WAITING'
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS waiting_decisions,

            COALESCE(
                SUM(
                    CASE
                        WHEN outcome = 'PURCHASED'
                             AND satisfaction IS NULL
                        THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS unevaluated_purchases
        FROM purchase_decision
        """;

    @Override
    public PurchaseAttentionSummary summarize() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SUMMARIZE);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            if (!resultSet.next()) {
                return new PurchaseAttentionSummary(0, 0);
            }

            return new PurchaseAttentionSummary(
                    resultSet.getLong(
                            "waiting_decisions"
                    ),
                    resultSet.getLong(
                            "unevaluated_purchases"
                    )
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar as pendências das decisões.",
                    exception
            );
        }
    }
}