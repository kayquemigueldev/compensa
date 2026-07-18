package com.kayque.compensa.goal.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.goal.model.SavingsGoalContribution;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SqliteSavingsGoalContributionRepository
        implements SavingsGoalContributionRepository {

    private static final int CURRENT_GOAL_ID = 1;

    private static final DateTimeFormatter
            DATABASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    private static final String UPDATE_GOAL_AMOUNT = """
        UPDATE savings_goal
        SET
            saved_amount = saved_amount + ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    private static final String INSERT_CONTRIBUTION = """
        INSERT INTO savings_goal_contribution (
            goal_id,
            amount,
            contributed_at
        )
        VALUES (?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_ALL = """
        SELECT
            id,
            amount,
            contributed_at
        FROM savings_goal_contribution
        WHERE goal_id = ?
        ORDER BY contributed_at DESC, id DESC
        """;

    private static final String FIND_AMOUNT_BY_ID = """
    SELECT amount
    FROM savings_goal_contribution
    WHERE id = ?
      AND goal_id = ?
    """;

    private static final String SUBTRACT_GOAL_AMOUNT = """
    UPDATE savings_goal
    SET
        saved_amount = saved_amount - ?,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = ?
      AND saved_amount >= ?
    """;

    private static final String DELETE_CONTRIBUTION = """
    DELETE FROM savings_goal_contribution
    WHERE id = ?
      AND goal_id = ?
    """;

    @Override
    public void add(BigDecimal amount) {
        validateAmount(amount);

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                updateGoalAmount(connection, amount);
                insertContribution(connection, amount);

                connection.commit();

            } catch (SQLException
                     | RuntimeException exception) {
                rollback(connection);
                throw exception;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível registrar a contribuição.",
                    exception
            );
        }
    }

    @Override
    public void remove(long contributionId) {
        if (contributionId <= 0) {
            throw new IllegalArgumentException(
                    "A contribuição informada é inválida."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                BigDecimal amount = findContributionAmount(
                        connection,
                        contributionId
                );

                subtractGoalAmount(connection, amount);

                deleteContribution(
                        connection,
                        contributionId
                );

                connection.commit();

            } catch (SQLException
                     | RuntimeException exception) {
                rollback(connection);
                throw exception;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível desfazer a contribuição.",
                    exception
            );
        }
    }

    @Override
    public List<SavingsGoalContribution> findAll() {
        List<SavingsGoalContribution> contributions =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_ALL)
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    contributions.add(
                            mapContribution(resultSet)
                    );
                }
            }

            return List.copyOf(contributions);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar as contribuições.",
                    exception
            );
        }
    }

    private void updateGoalAmount(
            Connection connection,
            BigDecimal amount
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                UPDATE_GOAL_AMOUNT
                        )
        ) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, CURRENT_GOAL_ID);

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "Crie um objetivo antes de adicionar uma contribuição."
                );
            }
        }
    }

    private void insertContribution(
            Connection connection,
            BigDecimal amount
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_CONTRIBUTION
                        )
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);
            statement.setBigDecimal(2, amount);
            statement.executeUpdate();
        }
    }

    private BigDecimal findContributionAmount(
            Connection connection,
            long contributionId
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_AMOUNT_BY_ID
                        )
        ) {
            statement.setLong(1, contributionId);
            statement.setInt(2, CURRENT_GOAL_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "A contribuição não foi encontrada."
                    );
                }

                return resultSet.getBigDecimal("amount");
            }
        }
    }

    private void subtractGoalAmount(
            Connection connection,
            BigDecimal amount
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                SUBTRACT_GOAL_AMOUNT
                        )
        ) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, CURRENT_GOAL_ID);
            statement.setBigDecimal(3, amount);

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "O saldo do objetivo não permite desfazer esta contribuição."
                );
            }
        }
    }

    private void deleteContribution(
            Connection connection,
            long contributionId
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                DELETE_CONTRIBUTION
                        )
        ) {
            statement.setLong(1, contributionId);
            statement.setInt(2, CURRENT_GOAL_ID);

            int deletedRows = statement.executeUpdate();

            if (deletedRows != 1) {
                throw new IllegalStateException(
                        "A contribuição não pôde ser removida."
                );
            }
        }
    }

    private SavingsGoalContribution mapContribution(
            ResultSet resultSet
    ) throws SQLException {
        return new SavingsGoalContribution(
                resultSet.getLong("id"),

                resultSet.getBigDecimal("amount"),

                LocalDateTime.parse(
                        resultSet.getString(
                                "contributed_at"
                        ),
                        DATABASE_DATE_FORMAT
                )
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "A contribuição deve ser maior que zero."
            );
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();

        } catch (SQLException rollbackException) {
            throw new IllegalStateException(
                    "Não foi possível desfazer a contribuição.",
                    rollbackException
            );
        }
    }
}