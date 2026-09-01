package ru.nstu.nest.files.service.kb;

import org.springframework.stereotype.Service;
import ru.nstu.nest.files.properties.loader.KnowledgebaseSourcePropertiesLoader;
import ru.nstu.nest.files.service.utility.FileSystemUtility;
import ru.nstu.nest.files.service.utility.KnowledgebaseSourceUtility;
import ru.nstu.nest.files.service.utility.SourceNameUtility;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.files.properties.kb.KnowledgebaseSourceProperties;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class KnowledgebaseSourceWriteService {

    private final KnowledgebaseSourcePropertiesLoader configLoader;

    public KnowledgebaseSourceWriteService(KnowledgebaseSourcePropertiesLoader configLoader) {
        this.configLoader = configLoader;
    }

    public void save(
            Path knowledgebaseDirectory,
            KnowledgebaseSourceFormData formData,
            KnowledgebaseSourceDescriptor existing
    ) {
        FileSystemUtility.createDirectories(knowledgebaseDirectory, "Не удалось создать каталог баз знаний");

        Path targetSourcePath = KnowledgebaseSourceUtility.resolveSourcePath(
                knowledgebaseDirectory,
                resolveSourceName(formData, existing)
        );
        FileSystemUtility.ensureNoConflicts(
                targetSourcePath,
                existing != null ? existing.integrationPath() : null,
                "База знаний с таким именем уже существует"
        );
        FileSystemUtility.createDirectories(targetSourcePath, "Не удалось создать каталог базы знаний");

        Path targetRulesPath = KnowledgebaseSourceUtility.resolveRulesPath(targetSourcePath);
        writeRulesFile(targetRulesPath, formData, existing);
        configLoader.save(KnowledgebaseSourceUtility.resolveConfigPath(targetSourcePath), toConfig(formData));
        FileSystemUtility.deleteObsoleteDirectory(
                existing != null ? existing.integrationPath() : null,
                targetSourcePath,
                existing != null
                        ? "Не удалось удалить " + existing.integrationPath().getFileName()
                        : "Не удалось удалить базу знаний"
        );
    }

    public void delete(KnowledgebaseSourceDescriptor descriptor) {
        FileSystemUtility.deleteRecursively(
                descriptor.integrationPath(),
                "Не удалось удалить " + descriptor.integrationPath().getFileName()
        );
    }

    public void rename(
            Path knowledgebaseDirectory,
            KnowledgebaseSourceDescriptor descriptor,
            String newName
    ) {
        FileSystemUtility.createDirectories(knowledgebaseDirectory, "Не удалось создать каталог баз знаний");

        Path targetSourcePath = KnowledgebaseSourceUtility.resolveSourcePath(knowledgebaseDirectory, newName);
        if (descriptor.integrationPath().equals(targetSourcePath)) {
            return;
        }

        FileSystemUtility.ensureNoConflicts(
                targetSourcePath,
                descriptor.integrationPath(),
                "База знаний с таким именем уже существует"
        );
        FileSystemUtility.move(
                descriptor.integrationPath(),
                targetSourcePath,
                "Не удалось переименовать " + descriptor.integrationPath().getFileName()
        );
    }

    private KnowledgebaseSourceProperties toConfig(KnowledgebaseSourceFormData formData) {
        KnowledgebaseSourceProperties properties = new KnowledgebaseSourceProperties();
        properties.setAgentType(formData.agentType());
        return properties;
    }

    private String resolveSourceName(
            KnowledgebaseSourceFormData formData,
            KnowledgebaseSourceDescriptor existing
    ) {
        if (formData.sourceRulesPath() != null) {
            return SourceNameUtility.extractStem(formData.sourceRulesPath());
        }
        if (formData.sourceName() != null && !formData.sourceName().isBlank()) {
            return formData.sourceName().trim();
        }
        if (existing != null) {
            return existing.name();
        }
        throw new IllegalStateException("Укажите имя базы знаний");
    }

    private void writeRulesFile(
            Path targetRulesPath,
            KnowledgebaseSourceFormData formData,
            KnowledgebaseSourceDescriptor existing
    ) {
        Path sourceRulesPath = formData.sourceRulesPath();

        if (sourceRulesPath != null) {
            if (!sourceRulesPath.equals(targetRulesPath)) {
                FileSystemUtility.copyFile(sourceRulesPath, targetRulesPath, "Не удалось сохранить базу знаний");
            }
            return;
        }

        if (existing != null) {
            if (!existing.rulesPath().equals(targetRulesPath)) {
                FileSystemUtility.copyFile(existing.rulesPath(), targetRulesPath, "Не удалось сохранить базу знаний");
            }
            return;
        }

        if (!Files.exists(targetRulesPath)) {
            FileSystemUtility.writeString(targetRulesPath, "", "Не удалось сохранить базу знаний");
        }
    }

}
