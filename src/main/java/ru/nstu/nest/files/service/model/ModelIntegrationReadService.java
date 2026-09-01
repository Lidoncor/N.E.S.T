package ru.nstu.nest.files.service.model;

import org.springframework.stereotype.Service;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.loader.ModelSourcePropertiesLoader;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.service.utility.ModelIntegrationUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ModelIntegrationReadService {

    private final ModelSourcePropertiesLoader configLoader;

    public ModelIntegrationReadService(ModelSourcePropertiesLoader configLoader) {
        this.configLoader = configLoader;
    }

    public List<Path> listConfigPaths(Path modelDirectory) {
        return listIntegrationPaths(modelDirectory).stream()
                .map(ModelIntegrationUtility::requireConfigPath)
                .toList();
    }

    public List<Path> listIntegrationPaths(Path modelDirectory) {
        if (modelDirectory == null || !Files.isDirectory(modelDirectory)) {
            return List.of();
        }

        try (Stream<Path> entries = Files.list(modelDirectory)) {
            return entries
                    .filter(ModelIntegrationUtility::isIntegrationDirectory)
                    .sorted(Comparator.comparing(ModelIntegrationUtility::resolveName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать каталог моделей", e);
        }
    }

    public List<ModelIntegrationDescriptor> list(Path modelDirectory) {
        return listIntegrationPaths(modelDirectory).stream()
                .map(this::toDescriptor)
                .toList();
    }

    private ModelIntegrationDescriptor toDescriptor(Path integrationPath) {
        Path configPath = ModelIntegrationUtility.requireConfigPath(integrationPath);
        ModelSourceProperties config = configLoader.load(configPath);
        return new ModelIntegrationDescriptor(
                ModelIntegrationUtility.resolveName(integrationPath),
                integrationPath,
                ModelIntegrationUtility.resolveModelPath(integrationPath),
                configPath,
                config
        );
    }

}
