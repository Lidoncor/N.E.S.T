package ru.nstu.nest.files.service.utility;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;

@UtilityClass
public class SourceNameUtility {

    public String extractStem(Path path) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
    }

    public String normalizeName(String rawName, String subject) {
        String normalized = rawName.trim().replaceAll("[\\\\/:*?\"<>|]+", "_");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Имя " + subject + " не должно быть пустым");
        }
        return normalized;
    }

}
