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
}