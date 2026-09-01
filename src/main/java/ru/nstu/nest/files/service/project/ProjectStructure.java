package ru.nstu.nest.files.service.project;

import java.io.File;
import java.nio.file.Path;

public record ProjectStructure(
        Path rootPath,
        String name,
        Path knowledgebasePath,
        Path modelPath,
        Path runtimePath
) {

    public static ProjectStructure from(File root) {
        Path rootPath = root.toPath();

        return new ProjectStructure(
                rootPath,
                root.getName(),
                rootPath.resolve("knowledgebase"),
                rootPath.resolve("model"),
                rootPath.resolve("runtime")
        );
    }

    public Path settingsPath() {
        return rootPath.resolve("settings.yml");
    }

}

