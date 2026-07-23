package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SqliteSavingsGoalTimelineRepository
        implements SavingsGoalTimelineRepository {

    private static final int CURRENT_GOAL_ID = 1;

    private static final DateTimeFormatter DATABASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    private static final String FIND_CREATED_DATE = """
        SELECT created_at
        FROM savings_goal
        WHERE id = ?
        """;

    @Override
    public Optional<LocalDate> findCreatedDate() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_CREATED_DATE
                        )
        ) {
            statement.setInt(1, CURRENT_GOAL_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String createdAt =
                        resultSet.getString("created_at");

                if (createdAt == null || createdAt.isBlank()) {
                    return Optional.empty();
                }

                return Optional.of(
                        LocalDateTime.parse(
                                createdAt,
                                DATABASE_DATE_FORMAT
                        ).toLocalDate()
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o início do objetivo.",
                    exception
            );
        }
    }
}