package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

public class SqliteSmartAlertSnoozeRepository
        implements SmartAlertSnoozeRepository {

    private static final String SAVE_SNOOZE = """
        INSERT INTO smart_alert_snooze (
            alert_code,
            snoozed_until,
            updated_at
        )
        VALUES (?, ?, CURRENT_TIMESTAMP)
        ON CONFLICT(alert_code) DO UPDATE SET
            snoozed_until = excluded.snoozed_until,
            updated_at = CURRENT_TIMESTAMP
        """;

    private static final String CHECK_SNOOZE = """
        SELECT 1
        FROM smart_alert_snooze
        WHERE alert_code = ?
          AND snoozed_until > ?
        LIMIT 1
        """;

    private static final String DELETE_EXPIRED = """
        DELETE FROM smart_alert_snooze
        WHERE snoozed_until <= ?
        """;

    @Override
    public void save(
            String alertCode,
            Instant snoozedUntil
    ) {
        String validatedCode = requireAlertCode(alertCode);

        Objects.requireNonNull(
                snoozedUntil,
                "O término do adiamento é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SAVE_SNOOZE)
        ) {
            statement.setString(1, validatedCode);
            statement.setString(2, snoozedUntil.toString());
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível adiar o alerta.",
                    exception
            );
        }
    }

    @Override
    public boolean isSnoozed(
            String alertCode,
            Instant currentInstant
    ) {
        String validatedCode = requireAlertCode(alertCode);

        Objects.requireNonNull(
                currentInstant,
                "O instante atual é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(CHECK_SNOOZE)
        ) {
            statement.setString(1, validatedCode);
            statement.setString(2, currentInstant.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível consultar o adiamento do alerta.",
                    exception
            );
        }
    }

    @Override
    public void deleteExpired(Instant currentInstant) {
        Objects.requireNonNull(
                currentInstant,
                "O instante atual é obrigatório."
        );

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(DELETE_EXPIRED)
        ) {
            statement.setString(
                    1,
                    currentInstant.toString()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível limpar os adiamentos expirados.",
                    exception
            );
        }
    }

    private String requireAlertCode(String alertCode) {
        if (alertCode == null || alertCode.isBlank()) {
            throw new IllegalArgumentException(
                    "O código do alerta é obrigatório."
            );
        }

        return alertCode.trim();
    }
}