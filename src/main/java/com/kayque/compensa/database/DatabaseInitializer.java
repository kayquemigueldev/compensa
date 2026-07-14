package com.kayque.compensa.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final String CREATE_FINANCIAL_PROFILE_TABLE = """
            CREATE TABLE IF NOT EXISTS financial_profile (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                net_monthly_income NUMERIC NOT NULL
                    CHECK (net_monthly_income > 0),
                monthly_work_hours NUMERIC NOT NULL
                    CHECK (monthly_work_hours > 0),
                monthly_additional_hours NUMERIC NOT NULL DEFAULT 0
                    CHECK (monthly_additional_hours >= 0),
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                Statement statement =
                        connection.createStatement()
        ) {
            statement.execute(CREATE_FINANCIAL_PROFILE_TABLE);

            System.out.println(
                    "Banco inicializado em: "
                            + DatabaseConnection.getDatabaseFile()
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o banco de dados.",
                    exception
            );
        }
    }
}