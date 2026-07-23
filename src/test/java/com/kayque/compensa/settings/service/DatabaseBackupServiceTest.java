package com.kayque.compensa.settings.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.nio.charset.StandardCharsets;

class DatabaseBackupServiceTest {

    private final DatabaseBackupService service =
            new DatabaseBackupService();

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldCreateDatabaseBackup() throws IOException {
        Path databaseFile =
                temporaryDirectory.resolve("compensa.db");

        Files.writeString(
                databaseFile,
                "conteúdo de teste"
        );

        Path backupDirectory =
                temporaryDirectory.resolve("backups");

        Path backupFile = service.createBackup(
                databaseFile,
                backupDirectory,
                LocalDateTime.of(
                        2026,
                        7,
                        19,
                        21,
                        30,
                        15
                )
        );

        assertEquals(
                "compensa-backup-20260719-213015.db",
                backupFile.getFileName().toString()
        );

        assertTrue(Files.exists(backupFile));

        assertEquals(
                "conteúdo de teste",
                Files.readString(backupFile)
        );
    }

    @Test
    void shouldRejectMissingDatabaseFile() {
        Path missingDatabase =
                temporaryDirectory.resolve("inexistente.db");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.createBackup(
                                missingDatabase,
                                temporaryDirectory,
                                LocalDateTime.now()
                        )
                );

        assertEquals(
                "O banco de dados ainda não está disponível.",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotOverwriteExistingBackup()
            throws IOException {

        Path databaseFile =
                temporaryDirectory.resolve("compensa.db");

        Files.writeString(databaseFile, "banco");

        LocalDateTime backupDate =
                LocalDateTime.of(
                        2026,
                        7,
                        19,
                        21,
                        30,
                        15
                );

        service.createBackup(
                databaseFile,
                temporaryDirectory,
                backupDate
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.createBackup(
                        databaseFile,
                        temporaryDirectory,
                        backupDate
                )
        );
    }

    @Test
    void shouldRestoreDatabaseAndCreateSafetyBackup()
            throws IOException {

        byte[] currentDatabaseContent =
                createSqliteContent("banco atual");

        byte[] backupContent =
                createSqliteContent("dados restaurados");

        Path databaseFile =
                temporaryDirectory.resolve("compensa.db");

        Path backupFile =
                temporaryDirectory.resolve(
                        "compensa-backup.db"
                );

        Files.write(
                databaseFile,
                currentDatabaseContent
        );

        Files.write(
                backupFile,
                backupContent
        );

        Path safetyBackup =
                service.restoreBackup(
                        backupFile,
                        databaseFile,
                        LocalDateTime.of(
                                2026,
                                7,
                                23,
                                17,
                                30,
                                15
                        )
                );

        assertTrue(Files.exists(safetyBackup));

        assertArrayEquals(
                currentDatabaseContent,
                Files.readAllBytes(safetyBackup)
        );

        assertArrayEquals(
                backupContent,
                Files.readAllBytes(databaseFile)
        );
    }

    @Test
    void shouldRejectInvalidBackupFile()
            throws IOException {

        Path databaseFile =
                temporaryDirectory.resolve("compensa.db");

        Path invalidBackup =
                temporaryDirectory.resolve(
                        "arquivo-invalido.db"
                );

        Files.write(
                databaseFile,
                createSqliteContent("banco atual")
        );

        Files.writeString(
                invalidBackup,
                "isto não é um banco SQLite"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.restoreBackup(
                        invalidBackup,
                        databaseFile,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectRestoringDatabaseFromItself()
            throws IOException {

        Path databaseFile =
                temporaryDirectory.resolve("compensa.db");

        Files.write(
                databaseFile,
                createSqliteContent("banco atual")
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.restoreBackup(
                                databaseFile,
                                databaseFile,
                                LocalDateTime.now()
                        )
                );

        assertEquals(
                "Escolha um arquivo de backup diferente do banco atual.",
                exception.getMessage()
        );
    }

    private byte[] createSqliteContent(String content) {
        return (
                "SQLite format 3\0"
                        + content
        ).getBytes(StandardCharsets.UTF_8);
    }

}