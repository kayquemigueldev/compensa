package com.kayque.compensa.database;

import java.sql.Connection;
import java.sql.ResultSet;
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
            essential_expenses NUMERIC NOT NULL DEFAULT 0
                CHECK (essential_expenses >= 0),
            monthly_savings_goal NUMERIC NOT NULL DEFAULT 0
                CHECK (monthly_savings_goal >= 0),
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

                satisfaction TEXT CHECK (
                    satisfaction IS NULL
                    OR satisfaction IN (
                        'WORTH_IT',
                        'PARTIALLY_WORTH_IT',
                        'REGRETTED'
                    )
                ),

                evaluated_at TEXT,

                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_USER_PROFILE_TABLE = """
        CREATE TABLE IF NOT EXISTS user_profile (
            id INTEGER PRIMARY KEY CHECK (id = 1),

            display_name TEXT NOT NULL
                CHECK (
                    length(trim(display_name))
                    BETWEEN 1 AND 50
                ),

            main_goal TEXT NOT NULL CHECK (
                main_goal IN (
                    'SAVE_MONEY',
                    'REDUCE_IMPULSE_PURCHASES',
                    'PLAN_A_PURCHASE',
                    'ORGANIZE_BUDGET'
                )
            ),

            recommendation_tone TEXT NOT NULL CHECK (
                recommendation_tone IN (
                    'GENTLE',
                    'BALANCED',
                    'DIRECT'
                )
            ),

            current_dream TEXT NOT NULL DEFAULT ''
                CHECK (length(current_dream) <= 120),

            updated_at TEXT NOT NULL
                DEFAULT CURRENT_TIMESTAMP
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
            statement.execute(CREATE_USER_PROFILE_TABLE);

            migrateFinancialProfileTable(connection);
            migratePurchaseDecisionTable(connection);

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

    private static void migrateFinancialProfileTable(
            Connection connection
    ) throws SQLException {
        addColumnIfMissing(
                connection,
                "financial_profile",
                "essential_expenses",
                """
                essential_expenses NUMERIC NOT NULL DEFAULT 0
                    CHECK (essential_expenses >= 0)
                """
        );

        addColumnIfMissing(
                connection,
                "financial_profile",
                "monthly_savings_goal",
                """
                monthly_savings_goal NUMERIC NOT NULL DEFAULT 0
                    CHECK (monthly_savings_goal >= 0)
                """
        );
    }

    private static void migratePurchaseDecisionTable(
            Connection connection
    ) throws SQLException {
        addColumnIfMissing(
                connection,
                "purchase_decision",
                "satisfaction",
                """
                satisfaction TEXT CHECK (
                    satisfaction IS NULL
                    OR satisfaction IN (
                        'WORTH_IT',
                        'PARTIALLY_WORTH_IT',
                        'REGRETTED'
                    )
                )
                """
        );

        addColumnIfMissing(
                connection,
                "purchase_decision",
                "evaluated_at",
                "evaluated_at TEXT"
        );
    }

    private static void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String definition
    ) throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }

        String sql = "ALTER TABLE "
                + table
                + " ADD COLUMN "
                + definition;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        System.out.println(
                "Migração aplicada: "
                        + table
                        + "."
                        + column
        );
    }

    private static boolean columnExists(
            Connection connection,
            String table,
            String expectedColumn
    ) throws SQLException {
        String sql = "PRAGMA table_info(" + table + ")";

        try (
                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                String columnName =
                        resultSet.getString("name");

                if (expectedColumn.equals(columnName)) {
                    return true;
                }
            }
        }

        return false;
    }
}