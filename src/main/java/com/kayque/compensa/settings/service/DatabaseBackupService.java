package com.kayque.compensa.settings.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DatabaseBackupService {

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd-HHmmss"
            );

    public Path createBackup(
            Path databaseFile,
            Path destinationDirectory,
            LocalDateTime backupDate
    ) {
        Objects.requireNonNull(
                databaseFile,
                "O arquivo do banco de dados é obrigatório."
        );

        Objects.requireNonNull(
                destinationDirectory,
                "A pasta de destino é obrigatória."
        );

        Objects.requireNonNull(
                backupDate,
                "A data do backup é obrigatória."
        );

        if (!Files.isRegularFile(databaseFile)) {
            throw new IllegalArgumentException(
                    "O banco de dados ainda não está disponível."
            );
        }

        try {
            Files.createDirectories(destinationDirectory);

            String fileName =
                    "compensa-backup-"
                            + backupDate.format(FILE_DATE_FORMAT)
                            + ".db";

            Path backupFile =
                    destinationDirectory.resolve(fileName);

            return Files.copy(
                    databaseFile,
                    backupFile
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível criar o backup.",
                    exception
            );
        }
    }
}