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
import com.kayque.compensa.purchase.model.PurchaseSatisfaction;

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
            satisfaction,
            created_at
        FROM purchase_decision
        ORDER BY created_at DESC, id DESC
        """;

    private static final String FIND_WAITING_DECISIONS = """
        SELECT
            id,
            product_name,
            price,
            real_work_minutes,
            advice_status,
            outcome,
            satisfaction,
            created_at
        FROM purchase_decision
        WHERE outcome = 'WAITING'
        ORDER BY created_at DESC, id DESC
        """;

    private static final String FINALIZE_WAITING_DECISION = """
        UPDATE purchase_decision
        SET outcome = ?
        WHERE id = ?
          AND outcome = 'WAITING'
        """;

    private static final String EVALUATE_PURCHASED_DECISION = """
        UPDATE purchase_decision
        SET
            satisfaction = ?,
            evaluated_at = CURRENT_TIMESTAMP
        WHERE id = ?
          AND outcome = 'PURCHASED'
        """;

    private static final String SUM_PURCHASED_AMOUNT_BETWEEN = """
        SELECT COALESCE(SUM(price), 0)
            AS purchased_amount
        FROM purchase_decision
        WHERE outcome = 'PURCHASED'
          AND created_at >= ?
          AND created_at < ?
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

    @Override
    public List<PurchaseDecisionHistoryItem> findWaiting() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_WAITING_DECISIONS
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            List<PurchaseDecisionHistoryItem> waitingDecisions =
                    new ArrayList<>();

            while (resultSet.next()) {
                waitingDecisions.add(
                        mapHistoryItem(resultSet)
                );
            }

            return List.copyOf(waitingDecisions);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar a lista de desejos.",
                    exception
            );
        }
    }

    @Override
    public boolean finalizeWaitingDecision(
            long decisionId,
            PurchaseDecisionOutcome finalOutcome
    ) {
        validateFinalization(decisionId, finalOutcome);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FINALIZE_WAITING_DECISION
                        )
        ) {
            statement.setString(
                    1,
                    finalOutcome.name()
            );

            statement.setLong(
                    2,
                    decisionId
            );

            int updatedRows = statement.executeUpdate();

            return updatedRows == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível finalizar a decisão.",
                    exception
            );
        }

    }

    @Override
    public boolean evaluatePurchasedDecision(
            long decisionId,
            PurchaseSatisfaction satisfaction
    ) {
        validateEvaluation(decisionId, satisfaction);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                EVALUATE_PURCHASED_DECISION
                        )
        ) {
            statement.setString(
                    1,
                    satisfaction.name()
            );

            statement.setLong(
                    2,
                    decisionId
            );

            int updatedRows = statement.executeUpdate();

            return updatedRows == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível avaliar a compra.",
                    exception
            );
        }
    }

    @Override
    public BigDecimal sumPurchasedAmountBetween(
            Instant startInclusive,
            Instant endExclusive
    ) {
        validatePeriod(startInclusive, endExclusive);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                SUM_PURCHASED_AMOUNT_BETWEEN
                        )
        ) {
            statement.setString(
                    1,
                    formatDatabaseDate(startInclusive)
            );

            statement.setString(
                    2,
                    formatDatabaseDate(endExclusive)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return BigDecimal.ZERO;
                }

                return new BigDecimal(
                        resultSet.getString(
                                "purchased_amount"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível calcular as compras do período.",
                    exception
            );
        }
    }

    private void validatePeriod(
            Instant startInclusive,
            Instant endExclusive
    ) {
        if (startInclusive == null
                || endExclusive == null) {
            throw new IllegalArgumentException(
                    "O período das compras é obrigatório."
            );
        }

        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException(
                    "O início do período deve ser anterior ao fim."
            );
        }
    }

    private String formatDatabaseDate(Instant instant) {
        return DATABASE_DATE_FORMAT.format(
                LocalDateTime.ofInstant(
                        instant,
                        ZoneOffset.UTC
                )
        );
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
                parseSatisfaction(
                        resultSet.getString("satisfaction")
                ),
                parseDatabaseDate(
                        resultSet.getString("created_at")
                )
        );
    }

    private PurchaseSatisfaction parseSatisfaction(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return PurchaseSatisfaction.valueOf(value);
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

    private void validateFinalization(
            long decisionId,
            PurchaseDecisionOutcome finalOutcome
    ) {
        if (decisionId <= 0) {
            throw new IllegalArgumentException(
                    "O ID da decisão deve ser positivo."
            );
        }

        if (finalOutcome == null) {
            throw new IllegalArgumentException(
                    "O resultado final é obrigatório."
            );
        }

        if (finalOutcome == PurchaseDecisionOutcome.WAITING) {
            throw new IllegalArgumentException(
                    "Uma decisão aguardando só pode ser finalizada como comprada ou recusada."
            );
        }
    }

    private void validateEvaluation(
            long decisionId,
            PurchaseSatisfaction satisfaction
    ) {
        if (decisionId <= 0) {
            throw new IllegalArgumentException(
                    "O ID da decisão deve ser positivo."
            );
        }

        if (satisfaction == null) {
            throw new IllegalArgumentException(
                    "A avaliação da compra é obrigatória."
            );
        }
    }
}