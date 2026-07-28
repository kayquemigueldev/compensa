package com.kayque.compensa.onboarding.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class SqliteAppPreferenceRepository
        implements AppPreferenceRepository {

    private static final String FIND_VALUE = """
        SELECT preference_value
        FROM app_preference
        WHERE preference_key = ?
        """;

    private static final String SAVE_VALUE = """
        INSERT INTO app_preference (
            preference_key,
            preference_value,
            updated_at
        )
        VALUES (?, ?, CURRENT_TIMESTAMP)
        ON CONFLICT(preference_key) DO UPDATE SET
            preference_value = excluded.preference_value,
            updated_at = CURRENT_TIMESTAMP
        """;

    @Override
    public Optional<String> findValue(String key) {
        validateKey(key);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(FIND_VALUE)
        ) {
            statement.setString(1, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        resultSet.getString(
                                "preference_value"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar a preferência do aplicativo.",
                    exception
            );
        }
    }

    @Override
    public void save(String key, String value) {
        validateKey(key);

        Objects.requireNonNull(
                value,
                "O valor da preferência é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SAVE_VALUE)
        ) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar a preferência do aplicativo.",
                    exception
            );
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "A chave da preferência é obrigatória."
            );
        }
    }
}
