package com.kayque.compensa.purchase.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.purchase.model.Purchase;
import com.kayque.compensa.purchase.model.PurchaseAdvice;
import com.kayque.compensa.purchase.model.PurchaseAnalysis;
import com.kayque.compensa.purchase.model.PurchaseDecision;
import com.kayque.compensa.purchase.model.PurchaseDecisionContext;
import com.kayque.compensa.purchase.model.PurchaseDecisionHistoryItem;
import com.kayque.compensa.purchase.model.PurchaseDecisionOutcome;
import com.kayque.compensa.purchase.model.PurchaseDecisionStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SqlitePurchaseDecisionRepository
        implements PurchaseDecisionRepository {

    private static final DateTimeFormatter DATABASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

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

    private static final String FIND_ALL_DECISIONS = """
            SELECT
                id,
                product_name,
                price,
                real_work_minutes,
                advice_status,
                outcome,
                created_at
            FROM purchase_decision
            ORDER BY created_at DESC, id DESC
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

    @Override
    public List<PurchaseDecisionHistoryItem> findAll() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_ALL_DECISIONS
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            List<PurchaseDecisionHistoryItem> history =
                    new ArrayList<>();

            while (resultSet.next()) {
                history.add(mapHistoryItem(resultSet));
            }

            return List.copyOf(history);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o histórico.",
                    exception
            );
        }
    }

    private PurchaseDecisionHistoryItem mapHistoryItem(
            ResultSet resultSet
    ) throws SQLException {
        return new PurchaseDecisionHistoryItem(
                resultSet.getLong("id"),
                resultSet.getString("product_name"),
                new BigDecimal(
                        resultSet.getString("price")
                ),
                resultSet.getLong("real_work_minutes"),
                PurchaseDecisionStatus.valueOf(
                        resultSet.getString("advice_status")
                ),
                PurchaseDecisionOutcome.valueOf(
                        resultSet.getString("outcome")
                ),
                parseDatabaseDate(
                        resultSet.getString("created_at")
                )
        );
    }

    private Instant parseDatabaseDate(String value) {
        LocalDateTime dateTime = LocalDateTime.parse(
                value,
                DATABASE_DATE_FORMAT
        );

        return dateTime.toInstant(ZoneOffset.UTC);
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