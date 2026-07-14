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

    private static final String CREATE_PURCHASE_DECISION_TABLE = """
            CREATE TABLE IF NOT EXISTS purchase_decision (
                id INTEGER PRIMARY KEY AUTOINCREMENT,

                product_name TEXT NOT NULL,
                price NUMERIC NOT NULL CHECK (price > 0),

                frequency TEXT NOT NULL CHECK (
                    frequency IN (
                        'ONCE',
                        'MONTHLY',
                        'WEEKLY',
                        'DAILY'
                    )
                ),

                planned INTEGER NOT NULL CHECK (
                    planned IN (0, 1)
                ),

                has_alternative INTEGER NOT NULL CHECK (
                    has_alternative IN (0, 1)
                ),

                urgent INTEGER NOT NULL CHECK (
                    urgent IN (0, 1)
                ),

                motivation TEXT NOT NULL CHECK (
                    motivation IN (
                        'NEED',
                        'DESIRE',
                        'IMPULSE'
                    )
                ),

                professional_work_minutes INTEGER NOT NULL
                    CHECK (professional_work_minutes >= 0),

                real_work_minutes INTEGER NOT NULL
                    CHECK (real_work_minutes >= 0),

                projected_yearly_cost NUMERIC NOT NULL
                    CHECK (projected_yearly_cost >= 0),

                advice_status TEXT NOT NULL CHECK (
                    advice_status IN (
                        'MAKES_SENSE',
                        'THINK_AGAIN',
                        'PROBABLY_NOT_WORTH_IT'
                    )
                ),

                advice_score INTEGER NOT NULL CHECK (
                    advice_score BETWEEN 0 AND 100
                ),

                advice_reasons TEXT NOT NULL,

                outcome TEXT NOT NULL CHECK (
                    outcome IN (
                        'PURCHASED',
                        'WAITING',
                        'DECLINED'
                    )
                ),

                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
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
            statement.execute(CREATE_PURCHASE_DECISION_TABLE);

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