package com.kayque.compensa.goal.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.goal.model.SavingsGoalAchievement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SqliteSavingsGoalAchievementRepository
        implements SavingsGoalAchievementRepository {

    private static final DateTimeFormatter
            DATABASE_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    private static final String FIND_ALL = """
        SELECT
            id,
            name,
            target_amount,
            saved_amount,
            target_date,
            created_at,
            completed_at
        FROM savings_goal_history
        ORDER BY completed_at DESC, id DESC
        """;

    @Override
    public List<SavingsGoalAchievement> findAll() {
        List<SavingsGoalAchievement> achievements =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_ALL);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                achievements.add(
                        mapAchievement(resultSet)
                );
            }

            return List.copyOf(achievements);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar as conquistas concluídas.",
                    exception
            );
        }
    }

    private SavingsGoalAchievement mapAchievement(
            ResultSet resultSet
    ) throws SQLException {
        return new SavingsGoalAchievement(
                resultSet.getLong("id"),

                resultSet.getString("name"),

                resultSet.getBigDecimal(
                        "target_amount"
                ),

                resultSet.getBigDecimal(
                        "saved_amount"
                ),

                parseOptionalDate(
                        resultSet.getString(
                                "target_date"
                        )
                ),

                parseDateTime(
                        resultSet.getString(
                                "created_at"
                        )
                ),

                parseDateTime(
                        resultSet.getString(
                                "completed_at"
                        )
                )
        );
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(
                value,
                DATABASE_DATE_TIME_FORMAT
        );
    }
}