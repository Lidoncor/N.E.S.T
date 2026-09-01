package ru.nstu.nest.files.service.utility;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public class ModelIntegrationUtility {

    private static final String MODEL_FILE_NAME = "model.onnx";
    private static final String CONFIG_YML = "config.yml";
    private static final String CONFIG_YAML = "config.yaml";

    public Optional<Path> findConfigPath(Path directory) {
        Path configYml = directory.resolve(CONFIG_YML);
        if (Files.isRegularFile(configYml)) {
            return Optional.of(configYml);
        }

        Path configYaml = directory.resolve(CONFIG_YAML);
        if (Files.isRegularFile(configYaml)) {
            return Optional.of(configYaml);
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(ModelIntegrationUtility::isYamlFile)
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать каталог модели", e);
        }
    }

    public Path requireConfigPath(Path integrationPath) {
        return findConfigPath(integrationPath)
                .orElseThrow(() -> new IllegalStateException(
                        "Параметры модели отсутствуют для " + integrationPath.getFileName()
                ));
    }

    public Path resolveModelPath(Path integrationPath) {
        Path modelPath = integrationPath.resolve(MODEL_FILE_NAME);
        if (!Files.isRegularFile(modelPath)) {
            throw new IllegalStateException("Файл модели отсутствует для " + integrationPath.getFileName());
        }
        return modelPath;
    }

    public String resolveName(Path integrationPath) {
        return integrationPath.getFileName().toString();
    }

    public String normalizeName(String rawName) {
        return SourceNameUtility.normalizeName(rawName, "модели");
    }

    public Path resolveConfigPath(Path integrationPath) {
        return integrationPath.resolve(CONFIG_YML);
    }

    public Path resolveManagedModelPath(Path integrationPath) {
        return integrationPath.resolve(MODEL_FILE_NAME);
    }

    public boolean isIntegrationDirectory(Path path) {
        return Files.isDirectory(path)
                && findConfigPath(path).isPresent()
                && Files.isRegularFile(resolveManagedModelPath(path));
    }

    private boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

}
