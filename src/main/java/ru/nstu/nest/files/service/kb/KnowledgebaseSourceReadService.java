package ru.nstu.nest.files.service.kb;

import org.springframework.stereotype.Service;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.files.properties.loader.KnowledgebaseSourcePropertiesLoader;
import ru.nstu.nest.files.service.utility.KnowledgebaseSourceUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class KnowledgebaseSourceReadService {

    private final KnowledgebaseSourcePropertiesLoader configLoader;

    public KnowledgebaseSourceReadService(KnowledgebaseSourcePropertiesLoader configLoader) {
        this.configLoader = configLoader;
    }

    public List<Path> listSourcePaths(Path knowledgebaseDirectory) {
        if (knowledgebaseDirectory == null || !Files.isDirectory(knowledgebaseDirectory)) {
            return List.of();
        }

        try (Stream<Path> entries = Files.list(knowledgebaseDirectory)) {
            return entries
                    .filter(KnowledgebaseSourceUtility::isSourceDirectory)
                    .sorted(Comparator.comparing(KnowledgebaseSourceUtility::resolveName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать каталог баз знаний", e);
        }
    }

    public List<KnowledgebaseSourceDescriptor> list(Path knowledgebaseDirectory) {
        return listSourcePaths(knowledgebaseDirectory).stream()
                .map(this::toDescriptor)
                .toList();
    }

    private KnowledgebaseSourceDescriptor toDescriptor(Path sourcePath) {
        Path configPath = KnowledgebaseSourceUtility.resolveConfigPath(sourcePath);
        return new KnowledgebaseSourceDescriptor(
                KnowledgebaseSourceUtility.resolveName(sourcePath),
                sourcePath,
                KnowledgebaseSourceUtility.resolveRulesPath(sourcePath),
                configPath,
                configLoader.load(configPath)
        );
    }

}
