package com.kayque.compensa.goal.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.goal.model.SavingsGoal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class SqliteSavingsGoalRepository
        implements SavingsGoalRepository {

    private static final int CURRENT_GOAL_ID = 1;

    private static final String FIND_GOAL = """
        SELECT
            name,
            target_amount,
            saved_amount,
            target_date
        FROM savings_goal
        WHERE id = ?
        """;

    private static final String SAVE_GOAL = """
        INSERT INTO savings_goal (
            id,
            name,
            target_amount,
            saved_amount,
            target_date,
            created_at,
            updated_at
        )
        VALUES (
            ?,
            ?,
            ?,
            ?,
            ?,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        ON CONFLICT(id) DO UPDATE SET
            name = excluded.name,
            target_amount = excluded.target_amount,
            saved_amount = excluded.saved_amount,
            target_date = excluded.target_date,
            updated_at = CURRENT_TIMESTAMP
        """;

    @Override
    public Optional<SavingsGoal> find() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_GOAL)
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapGoal(resultSet));
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o objetivo.",
                    exception
            );
        }
    }

    @Override
    public void save(SavingsGoal goal) {
        Objects.requireNonNull(
                goal,
                "O objetivo é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SAVE_GOAL)
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);
            statement.setString(2, goal.name());

            statement.setBigDecimal(
                    3,
                    goal.targetAmount()
            );

            statement.setBigDecimal(
                    4,
                    goal.savedAmount()
            );

            if (goal.hasTargetDate()) {
                statement.setString(
                        5,
                        goal.targetDate().toString()
                );
            } else {
                statement.setNull(
                        5,
                        Types.VARCHAR
                );
            }

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar o objetivo.",
                    exception
            );
        }
    }

    private SavingsGoal mapGoal(
            ResultSet resultSet
    ) throws SQLException {
        String targetDateValue =
                resultSet.getString("target_date");

        LocalDate targetDate =
                targetDateValue == null
                        ? null
                        : LocalDate.parse(targetDateValue);

        return new SavingsGoal(
                resultSet.getString("name"),

                resultSet.getBigDecimal(
                        "target_amount"
                ),

                resultSet.getBigDecimal(
                        "saved_amount"
                ),

                targetDate
        );
    }
}