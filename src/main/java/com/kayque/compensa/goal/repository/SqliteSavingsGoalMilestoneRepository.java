package com.kayque.compensa.goal.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteSavingsGoalMilestoneRepository
        implements SavingsGoalMilestoneRepository {

    private static final int CURRENT_GOAL_ID = 1;

    private static final String FIND_LAST_MILESTONE = """
        SELECT last_celebrated_milestone
        FROM savings_goal
        WHERE id = ?
        """;

    private static final String UPDATE_LAST_MILESTONE = """
        UPDATE savings_goal
        SET
            last_celebrated_milestone = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    @Override
    public int findLastCelebratedMilestone() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_LAST_MILESTONE
                        )
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }

                return resultSet.getInt(
                        "last_celebrated_milestone"
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o último marco celebrado.",
                    exception
            );
        }
    }

    @Override
    public void saveLastCelebratedMilestone(
            int milestone
    ) {
        if (milestone != 0
                && milestone != 25
                && milestone != 50
                && milestone != 75
                && milestone != 100) {
            throw new IllegalArgumentException(
                    "O marco informado é inválido."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                UPDATE_LAST_MILESTONE
                        )
        ) {
            statement.setInt(1, milestone);
            statement.setInt(2, CURRENT_GOAL_ID);

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "Nenhum objetivo foi encontrado."
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar o marco celebrado.",
                    exception
            );
        }
    }
}