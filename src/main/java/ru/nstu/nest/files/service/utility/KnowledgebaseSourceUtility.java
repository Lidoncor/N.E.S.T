package ru.nstu.nest.files.service.utility;

import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class KnowledgebaseSourceUtility {

    private static final String RULES_FILE_NAME = "rules.klb";
    private static final String CONFIG_FILE_NAME = "config.yml";

    public String resolveName(Path sourcePath) {
        return sourcePath.getFileName().toString();
    }

    public Path resolveConfigPath(Path sourcePath) {
        return sourcePath.resolve(CONFIG_FILE_NAME);
    }

    public Path resolveRulesPath(Path sourcePath) {
        return sourcePath.resolve(RULES_FILE_NAME);
    }

    public Path resolveSourcePath(Path knowledgebaseDirectory, String sourceName) {
        return knowledgebaseDirectory.resolve(normalizeName(sourceName));
    }

    public String normalizeName(String rawName) {
        return SourceNameUtility.normalizeName(rawName, "базы знаний");
    }

    public boolean isSourceDirectory(Path path) {
        return Files.isDirectory(path) && hasRequiredFiles(path);
    }

    private boolean hasRequiredFiles(Path path) {
        return Files.isRegularFile(resolveRulesPath(path))
                && Files.isRegularFile(resolveConfigPath(path));
    }

}
