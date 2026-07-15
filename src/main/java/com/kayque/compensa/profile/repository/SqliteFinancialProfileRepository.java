package com.kayque.compensa.profile.repository;

import com.kayque.compensa.database.DatabaseConnection;
import com.kayque.compensa.profile.model.FinancialProfile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SqliteFinancialProfileRepository
        implements FinancialProfileRepository {

    private static final int PROFILE_ID = 1;

    private static final String FIND_PROFILE = """
            SELECT
                net_monthly_income,
                monthly_work_hours,
                monthly_additional_hours,
                essential_expenses,
                monthly_savings_goal
            FROM financial_profile
            WHERE id = ?
            """;

    private static final String SAVE_PROFILE = """
            INSERT INTO financial_profile (
                id,
                net_monthly_income,
                monthly_work_hours,
                monthly_additional_hours,
                essential_expenses,
                monthly_savings_goal,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(id) DO UPDATE SET
                net_monthly_income =
                    excluded.net_monthly_income,
                monthly_work_hours =
                    excluded.monthly_work_hours,
                monthly_additional_hours =
                    excluded.monthly_additional_hours,
                essential_expenses =
                    excluded.essential_expenses,
                monthly_savings_goal =
                    excluded.monthly_savings_goal,
                updated_at = CURRENT_TIMESTAMP
            """;

    @Override
    public Optional<FinancialProfile> find() {
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
                    "Não foi possível carregar o perfil financeiro.",
                    exception
            );
        }
    }

    @Override
    public void save(FinancialProfile profile) {
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(SAVE_PROFILE)
        ) {
            statement.setInt(1, PROFILE_ID);

            statement.setBigDecimal(
                    2,
                    profile.netMonthlyIncome()
            );

            statement.setBigDecimal(
                    3,
                    profile.monthlyWorkHours()
            );

            statement.setBigDecimal(
                    4,
                    profile.monthlyAdditionalHours()
            );

            statement.setBigDecimal(
                    5,
                    profile.essentialExpenses()
            );

            statement.setBigDecimal(
                    6,
                    profile.monthlySavingsGoal()
            );

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Não foi possível salvar o perfil financeiro.",
                    exception
            );
        }
    }

    private FinancialProfile mapProfile(
            ResultSet resultSet
    ) throws SQLException {
        return new FinancialProfile(
                readBigDecimal(
                        resultSet,
                        "net_monthly_income"
                ),
                readBigDecimal(
                        resultSet,
                        "monthly_work_hours"
                ),
                readBigDecimal(
                        resultSet,
                        "monthly_additional_hours"
                ),
                readBigDecimal(
                        resultSet,
                        "essential_expenses"
                ),
                readBigDecimal(
                        resultSet,
                        "monthly_savings_goal"
                )
        );
    }

    private BigDecimal readBigDecimal(
            ResultSet resultSet,
            String column
    ) throws SQLException {
        return new BigDecimal(
                resultSet.getString(column)
        );
    }
}