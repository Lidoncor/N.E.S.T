package ru.nstu.nest.ui.view.dialog.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class DialogFieldValues {

    private DialogFieldValues() {
    }

    public static String requireValue(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }

    public static String optionalValue(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static String defaultValue(String value) {
        return value == null ? "" : value;
    }

    public static List<String> parseLines(String value, String errorMessage) {
        List<String> lines = Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return lines;
    }

    public static Path requireExistingFile(String pathText, String extension, String subject) {
        String normalizedPath = requireValue(pathText, subject + ": укажите путь");
        Path path = Path.of(normalizedPath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(subject + " не найден: " + normalizedPath);
        }
        if (!normalizedPath.toLowerCase().endsWith(extension.toLowerCase())) {
            throw new IllegalArgumentException("Поддерживаются только файлы " + extension);
        }
        return path;
    }

}
