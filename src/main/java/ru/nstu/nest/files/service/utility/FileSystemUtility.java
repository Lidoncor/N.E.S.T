package ru.nstu.nest.files.service.utility;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

@UtilityClass
public class FileSystemUtility {

    public void createDirectories(Path path, String errorMessage) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void copyFile(Path sourcePath, Path targetPath, String errorMessage) {
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void move(Path sourcePath, Path targetPath, String errorMessage) {
        try {
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void writeString(Path path, String content, String errorMessage) {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void deleteRecursively(Path path, String errorMessage) {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder()).forEach(FileSystemUtility::deleteIfExists);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void deleteIfExists(Path path, String errorMessage) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void ensureNoConflicts(Path targetPath, Path currentPath, String errorMessage) {
        if (Files.exists(targetPath) && !targetPath.equals(currentPath)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public void deleteObsoleteDirectory(Path currentPath, Path targetPath, String errorMessage) {
        if (currentPath == null || currentPath.equals(targetPath)) {
            return;
        }

        deleteRecursively(currentPath, errorMessage);
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось удалить " + path.getFileName(), e);
        }
    }

}
