package com.kayque.compensa.purchase.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlitePurchaseDecisionRepository
        implements PurchaseDecisionRepository {

    private static final String INSERT_DECISION = """
            INSERT INTO purchase_decision (
                product_name,
                price,
                frequency,
                planned,
                has_alternative,
                urgent,
                motivation,
                professional_work_minutes,
                real_work_minutes,
                projected_yearly_cost,
                advice_status,
                advice_score,
                advice_reasons,
                outcome
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    @Override
    public long save(PurchaseDecision decision) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_DECISION,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            fillStatement(statement, decision);
            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }

                throw new IllegalStateException(
                        "O banco não retornou o ID da decisão."
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar a decisão de compra.",
                    exception
            );
        }
    }

    private void fillStatement(
            PreparedStatement statement,
            PurchaseDecision decision
    ) throws SQLException {
        PurchaseAnalysis analysis = decision.analysis();
        Purchase purchase = analysis.purchase();
        PurchaseDecisionContext context = decision.context();
        PurchaseAdvice advice = decision.advice();

        statement.setString(1, purchase.name());
        statement.setBigDecimal(2, purchase.price());
        statement.setString(
                3,
                purchase.frequency().name()
        );

        statement.setInt(
                4,
                context.planned() ? 1 : 0
        );

        statement.setInt(
                5,
                context.hasAlternative() ? 1 : 0
        );

        statement.setInt(
                6,
                context.urgent() ? 1 : 0
        );

        statement.setString(
                7,
                context.motivation().name()
        );

        statement.setLong(
                8,
                analysis.professionalWorkMinutes()
        );

        statement.setLong(
                9,
                analysis.realWorkMinutes()
        );

        statement.setBigDecimal(
                10,
                analysis.projectedYearlyCost()
        );

        statement.setString(
                11,
                advice.status().name()
        );

        statement.setInt(
                12,
                advice.score()
        );

        statement.setString(
                13,
                String.join("\n", advice.reasons())
        );

        statement.setString(
                14,
                decision.outcome().name()
        );
    }
}