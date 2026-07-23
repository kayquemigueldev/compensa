package com.kayque.compensa.settings.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DatabaseBackupService {

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd-HHmmss"
            );

    private static final byte[] SQLITE_HEADER =
            "SQLite format 3\0".getBytes(
                    StandardCharsets.US_ASCII
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

        validateCurrentDatabase(databaseFile);

        Path normalizedDatabase =
                databaseFile.toAbsolutePath().normalize();

        Path normalizedDestination =
                destinationDirectory
                        .toAbsolutePath()
                        .normalize();

        String backupFileName =
                "compensa-backup-"
                        + backupDate.format(FILE_DATE_FORMAT)
                        + ".db";

        Path backupFile =
                normalizedDestination.resolve(backupFileName);

        try {
            Files.createDirectories(normalizedDestination);

            return Files.copy(
                    normalizedDatabase,
                    backupFile
            );

        } catch (FileAlreadyExistsException exception) {
            throw new IllegalStateException(
                    "Já existe um backup criado neste instante.",
                    exception
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível criar o backup do banco de dados.",
                    exception
            );
        }
    }
    public Path restoreBackup(
            Path backupFile,
            Path databaseFile,
            LocalDateTime restoreDate
    ) {
        Objects.requireNonNull(
                backupFile,
                "O arquivo de backup é obrigatório."
        );

        Objects.requireNonNull(
                databaseFile,
                "O destino do banco de dados é obrigatório."
        );

        Objects.requireNonNull(
                restoreDate,
                "A data da restauração é obrigatória."
        );

        validateBackupFile(backupFile);

        Path normalizedBackup =
                backupFile.toAbsolutePath().normalize();

        Path normalizedDatabase =
                databaseFile.toAbsolutePath().normalize();

        if (normalizedBackup.equals(normalizedDatabase)) {
            throw new IllegalArgumentException(
                    "Escolha um arquivo de backup diferente do banco atual."
            );
        }

        Path databaseDirectory =
                normalizedDatabase.getParent();

        if (databaseDirectory == null) {
            throw new IllegalArgumentException(
                    "O destino do banco de dados é inválido."
            );
        }

        try {
            Files.createDirectories(databaseDirectory);

            Path safetyBackup = createSafetyBackup(
                    normalizedDatabase,
                    databaseDirectory,
                    restoreDate
            );

            Path temporaryFile =
                    databaseDirectory.resolve(
                            "compensa-restore-temporary.db"
                    );

            Files.copy(
                    normalizedBackup,
                    temporaryFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            replaceDatabase(
                    temporaryFile,
                    normalizedDatabase
            );

            return safetyBackup;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível restaurar o backup.",
                    exception
            );
        }
    }

    public void validateBackupFile(Path backupFile) {
        Objects.requireNonNull(
                backupFile,
                "O arquivo de backup é obrigatório."
        );

        if (!Files.isRegularFile(backupFile)) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo de backup válido."
            );
        }

        try {
            byte[] fileHeader =
                    readSqliteHeader(backupFile);

            if (!hasValidSqliteHeader(fileHeader)) {
                throw new IllegalArgumentException(
                        "O arquivo selecionado não é um banco SQLite válido."
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível verificar o arquivo de backup.",
                    exception
            );
        }
    }

    private Path createSafetyBackup(
            Path databaseFile,
            Path databaseDirectory,
            LocalDateTime restoreDate
    ) throws IOException {
        String fileName =
                "compensa-before-restore-"
                        + restoreDate.format(FILE_DATE_FORMAT)
                        + ".db";

        Path safetyBackup =
                databaseDirectory.resolve(fileName);

        if (!Files.isRegularFile(databaseFile)) {
            return safetyBackup;
        }

        return Files.copy(
                databaseFile,
                safetyBackup,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    private void replaceDatabase(
            Path temporaryFile,
            Path databaseFile
    ) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    databaseFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    temporaryFile,
                    databaseFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void validateCurrentDatabase(Path databaseFile) {
        if (!Files.isRegularFile(databaseFile)) {
            throw new IllegalArgumentException(
                    "O banco de dados ainda não está disponível."
            );
        }
    }

    private byte[] readSqliteHeader(
            Path databaseFile
    ) throws IOException {
        try (var inputStream =
                     Files.newInputStream(databaseFile)) {
            return inputStream.readNBytes(
                    SQLITE_HEADER.length
            );
        }
    }

    private boolean hasValidSqliteHeader(
            byte[] fileHeader
    ) {
        if (fileHeader.length != SQLITE_HEADER.length) {
            return false;
        }

        for (int index = 0;
             index < SQLITE_HEADER.length;
             index++) {

            if (fileHeader[index] != SQLITE_HEADER[index]) {
                return false;
            }
        }

        return true;
    }
}