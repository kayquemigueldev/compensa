package com.kayque.compensa.goal.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteSavingsGoalLifecycleRepository
        implements SavingsGoalLifecycleRepository {

    private static final int CURRENT_GOAL_ID = 1;

    private static final String ARCHIVE_GOAL = """
        INSERT INTO savings_goal_history (
            name,
            target_amount,
            saved_amount,
            target_date,
            created_at,
            completed_at
        )
        SELECT
            name,
            target_amount,
            saved_amount,
            target_date,
            created_at,
            CURRENT_TIMESTAMP
        FROM savings_goal
        WHERE id = ?
          AND saved_amount >= target_amount
        """;

    private static final String FIND_ARCHIVED_GOAL_ID = """
        SELECT last_insert_rowid()
        """;

    private static final String
            ARCHIVE_CONTRIBUTIONS = """
        INSERT INTO savings_goal_contribution_history (
            achievement_id,
            amount,
            contributed_at
        )
        SELECT
            ?,
            amount,
            contributed_at
        FROM savings_goal_contribution
        WHERE goal_id = ?
        """;

    private static final String DELETE_CURRENT_GOAL = """
        DELETE FROM savings_goal
        WHERE id = ?
        """;

    @Override
    public void archiveCompletedGoalAndPrepareNew() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                archiveGoal(connection);

                long achievementId =
                        findArchivedGoalId(connection);

                archiveContributions(
                        connection,
                        achievementId
                );

                deleteCurrentGoal(connection);

                connection.commit();

            } catch (SQLException
                     | RuntimeException exception) {
                rollback(connection);
                throw exception;

            } finally {
                restoreAutoCommit(connection);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível preparar um novo objetivo.",
                    exception
            );
        }
    }

    private void archiveGoal(
            Connection connection
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                ARCHIVE_GOAL
                        )
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            int insertedRows = statement.executeUpdate();

            if (insertedRows != 1) {
                throw new IllegalStateException(
                        "O objetivo precisa estar concluído antes de iniciar outro."
                );
            }
        }
    }

    private long findArchivedGoalId(
            Connection connection
    ) throws SQLException {
        try (
                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(
                                FIND_ARCHIVED_GOAL_ID
                        )
        ) {
            if (!resultSet.next()) {
                throw new IllegalStateException(
                        "O histórico da conquista não foi gerado."
                );
            }

            long achievementId = resultSet.getLong(1);

            if (achievementId <= 0) {
                throw new IllegalStateException(
                        "O histórico da conquista não foi identificado."
                );
            }

            return achievementId;
        }
    }

    private void archiveContributions(
            Connection connection,
            long achievementId
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                ARCHIVE_CONTRIBUTIONS
                        )
        ) {
            statement.setLong(1, achievementId);
            statement.setInt(2, CURRENT_GOAL_ID);
            statement.executeUpdate();
        }
    }

    private void deleteCurrentGoal(
            Connection connection
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                DELETE_CURRENT_GOAL
                        )
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            int deletedRows = statement.executeUpdate();

            if (deletedRows != 1) {
                throw new IllegalStateException(
                        "O objetivo concluído não foi encontrado."
                );
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível desfazer o arquivamento.",
                    exception
            );
        }
    }

    private void restoreAutoCommit(
            Connection connection
    ) {
        try {
            connection.setAutoCommit(true);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível restaurar a conexão.",
                    exception
            );
        }
    }
}