package com.kayque.compensa.alerts.repository;

import com.kayque.compensa.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class SqliteSmartAlertReadRepository
        implements SmartAlertReadRepository {

    private static final String MARK_AS_READ = """
        INSERT INTO smart_alert_read (
            alert_code,
            read_at
        )
        VALUES (?, ?)
        ON CONFLICT(alert_code) DO UPDATE SET
            read_at = excluded.read_at
        """;

    private static final String CHECK_READ = """
        SELECT 1
        FROM smart_alert_read
        WHERE alert_code = ?
        LIMIT 1
        """;

    private static final String FIND_ALL_READ_CODES = """
        SELECT alert_code
        FROM smart_alert_read
        ORDER BY read_at DESC, alert_code ASC
        """;

    private static final String DELETE_READ_STATE = """
        DELETE FROM smart_alert_read
        WHERE alert_code = ?
        """;

    @Override
    public void markAsRead(
            String alertCode,
            Instant readAt
    ) {
        String validatedCode =
                requireAlertCode(alertCode);

        if (readAt == null) {
            throw new NullPointerException(
                    "A data de leitura é obrigatória."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                MARK_AS_READ
                        )
        ) {
            statement.setString(1, validatedCode);
            statement.setString(2, readAt.toString());
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível marcar o alerta como lido.",
                    exception
            );
        }
    }

    @Override
    public boolean isRead(String alertCode) {
        String validatedCode =
                requireAlertCode(alertCode);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                CHECK_READ
                        )
        ) {
            statement.setString(1, validatedCode);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {
                return resultSet.next();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível verificar a leitura do alerta.",
                    exception
            );
        }
    }

    @Override
    public Set<String> findAllReadCodes() {
        Set<String> codes = new HashSet<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_ALL_READ_CODES
                        );

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                codes.add(
                        resultSet.getString("alert_code")
                );
            }

            return Set.copyOf(codes);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar os alertas lidos.",
                    exception
            );
        }
    }

    @Override
    public void delete(String alertCode) {
        String validatedCode =
                requireAlertCode(alertCode);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                DELETE_READ_STATE
                        )
        ) {
            statement.setString(1, validatedCode);
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível restaurar o estado do alerta.",
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