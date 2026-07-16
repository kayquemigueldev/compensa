package com.kayque.compensa.userprofile.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.userprofile.model.RecommendationTone;
import com.kayque.compensa.userprofile.model.UserGoal;
import com.kayque.compensa.userprofile.model.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.math.BigDecimal;
import java.sql.Types;

public class SqliteUserProfileRepository
        implements UserProfileRepository {

    private static final int PROFILE_ID = 1;

    private static final String FIND_PROFILE = """
        SELECT
            display_name,
            main_goal,
            recommendation_tone,
            current_dream,
            current_dream_target_amount
        FROM user_profile
        WHERE id = ?
        """;

    private static final String SAVE_PROFILE = """
        INSERT INTO user_profile (
            id,
            display_name,
            main_goal,
            recommendation_tone,
            current_dream,
            current_dream_target_amount,
            updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        ON CONFLICT(id) DO UPDATE SET
            display_name = excluded.display_name,
            main_goal = excluded.main_goal,
            recommendation_tone =
                excluded.recommendation_tone,
            current_dream = excluded.current_dream,
            current_dream_target_amount =
                excluded.current_dream_target_amount,
            updated_at = CURRENT_TIMESTAMP
        """;

    @Override
    public Optional<UserProfile> find() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_PROFILE)
        ) {
            statement.setInt(1, PROFILE_ID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapProfile(resultSet));
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o perfil do usuário.",
                    exception
            );
        }
    }

    @Override
    public void save(UserProfile profile) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SAVE_PROFILE)
        ) {
            statement.setInt(1, PROFILE_ID);
            statement.setString(2, profile.displayName());
            statement.setString(3, profile.mainGoal().name());

            statement.setString(
                    4,
                    profile.recommendationTone().name()
            );

            statement.setString(5, profile.currentDream());

            if (profile.hasCurrentDreamTargetAmount()) {
                statement.setBigDecimal(
                        6,
                        profile.currentDreamTargetAmount()
                );
            } else {
                statement.setNull(6, Types.NUMERIC);
            }

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar o perfil do usuário.",
                    exception
            );
        }
    }

    private UserProfile mapProfile(
            ResultSet resultSet
    ) throws SQLException {
        return new UserProfile(
                resultSet.getString("display_name"),

                UserGoal.valueOf(
                        resultSet.getString("main_goal")
                ),

                RecommendationTone.valueOf(
                        resultSet.getString(
                                "recommendation_tone"
                        )
                ),

                resultSet.getString("current_dream"),

                resultSet.getBigDecimal(
                        "current_dream_target_amount"
                )
        );
    }
}