package com.kayque.compensa.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final Path DATABASE_DIRECTORY = Path.of(
            System.getProperty("user.home"),
            ".compensa"
    );

    private static final Path DATABASE_FILE = DATABASE_DIRECTORY.resolve(
            "compensa.db"
    );

    private static final String DATABASE_URL =
            "jdbc:sqlite:" + DATABASE_FILE;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        createDatabaseDirectory();

        Connection connection = DriverManager.getConnection(DATABASE_URL);

        enableForeignKeys(connection);

        return connection;
    }

    public static Path getDatabaseFile() {
        return DATABASE_FILE;
    }

    private static void createDatabaseDirectory() throws SQLException {
        try {
            Files.createDirectories(DATABASE_DIRECTORY);

        } catch (IOException exception) {
            throw new SQLException(
                    "Não foi possível criar a pasta do banco de dados.",
                    exception
            );
        }
    }

    private static void enableForeignKeys(
            Connection connection
    ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}