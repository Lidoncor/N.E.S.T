package ru.nstu.nest.files.service.model;

import org.springframework.stereotype.Service;
import ru.nstu.nest.files.properties.loader.ModelSourcePropertiesLoader;
import ru.nstu.nest.files.service.utility.FileSystemUtility;
import ru.nstu.nest.files.service.utility.ModelIntegrationUtility;
import ru.nstu.nest.files.service.utility.SourceNameUtility;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.ui.model.ModelIntegrationFormData;

import java.nio.file.Path;
import java.util.List;

@Service
public class ModelIntegrationWriteService {

    private final ModelSourcePropertiesLoader configLoader;

    public ModelIntegrationWriteService(ModelSourcePropertiesLoader configLoader) {
        this.configLoader = configLoader;
    }

    public void save(Path modelDirectory, ModelIntegrationFormData formData, ModelIntegrationDescriptor existing) {
        FileSystemUtility.createDirectories(modelDirectory, "Не удалось создать каталог моделей");

        Path targetIntegrationPath = modelDirectory.resolve(
                ModelIntegrationUtility.normalizeName(resolveIntegrationName(formData, existing))
        );
        Path targetModelPath = ModelIntegrationUtility.resolveManagedModelPath(targetIntegrationPath);
        Path targetConfigPath = ModelIntegrationUtility.resolveConfigPath(targetIntegrationPath);

        FileSystemUtility.ensureNoConflicts(
                targetIntegrationPath,
                existing != null ? existing.integrationPath() : null,
                "Модель с таким именем уже существует"
        );
        FileSystemUtility.createDirectories(targetIntegrationPath, "Не удалось создать каталог модели");
        copyModelFile(formData.sourceModelPath(), targetModelPath);
        configLoader.save(targetConfigPath, toConfig(formData));
        deleteObsoleteFiles(targetIntegrationPath, targetModelPath, targetConfigPath, existing);
    }

    public void delete(ModelIntegrationDescriptor descriptor) {
        FileSystemUtility.deleteRecursively(
                descriptor.integrationPath(),
                "Не удалось удалить " + descriptor.integrationPath().getFileName()
        );
    }

    public void rename(
            Path modelDirectory,
            ModelIntegrationDescriptor descriptor,
            String newName
    ) {
        FileSystemUtility.createDirectories(modelDirectory, "Не удалось создать каталог моделей");

        Path targetIntegrationPath = modelDirectory.resolve(ModelIntegrationUtility.normalizeName(newName));
        if (descriptor.integrationPath().equals(targetIntegrationPath)) {
            return;
        }

        FileSystemUtility.ensureNoConflicts(
                targetIntegrationPath,
                descriptor.integrationPath(),
                "Модель с таким именем уже существует"
        );
        FileSystemUtility.move(
                descriptor.integrationPath(),
                targetIntegrationPath,
                "Не удалось переименовать " + descriptor.integrationPath().getFileName()
        );
    }

    private ModelSourceProperties toConfig(ModelIntegrationFormData formData) {
        ModelSourceProperties properties = new ModelSourceProperties();
        properties.getModel().setTaskType(formData.taskType());
        properties.getTrigger().setFacts(List.copyOf(formData.triggerFacts()));
        properties.getInput().setTensorName(formData.inputTensorName());
        properties.getInput().setType(formData.inputType());
        properties.getInput().setFeatures(copyFeatures(formData.inputFeatures()));
        properties.getInput().setImage(copyImage(formData.imageInput()));
        properties.getOutput().setTensorName(blankToNull(formData.outputTensorName()));
        properties.getOutput().setClassMapping(copyClassMapping(formData.outputClassMapping()));
        properties.getOutput().getResultMapping().setFact(formData.resultFact());
        return properties;
    }

    private ModelSourceProperties.Image copyImage(ModelSourceProperties.Image image) {
        ModelSourceProperties.Image copy = new ModelSourceProperties.Image();
        if (image == null) {
            return copy;
        }
        copy.setFact(image.getFact());
        copy.setWidth(image.getWidth());
        copy.setHeight(image.getHeight());
        copy.setColorMode(image.getColorMode());
        copy.setLayout(image.getLayout());
        copy.setNormalize(image.isNormalize());
        return copy;
    }

    private List<ModelSourceProperties.Feature> copyFeatures(List<ModelSourceProperties.Feature> features) {
        return features.stream()
                .map(feature -> {
                    ModelSourceProperties.Feature copy = new ModelSourceProperties.Feature();
                    copy.setIndex(feature.getIndex());
                    copy.setFact(feature.getFact());
                    return copy;
                })
                .toList();
    }

    private List<ModelSourceProperties.ClassMapping> copyClassMapping(
            List<ModelSourceProperties.ClassMapping> classMapping
    ) {
        return classMapping.stream()
                .map(mapping -> {
                    ModelSourceProperties.ClassMapping copy = new ModelSourceProperties.ClassMapping();
                    copy.setIndex(mapping.getIndex());
                    copy.setLabel(mapping.getLabel());
                    copy.setFact(mapping.getFact());
                    return copy;
                })
                .toList();
    }

    private String resolveIntegrationName(
            ModelIntegrationFormData formData,
            ModelIntegrationDescriptor existing
    ) {
        if (formData.sourceModelPath() != null) {
            return SourceNameUtility.extractStem(formData.sourceModelPath());
        }
        if (existing != null) {
            return existing.name();
        }
        throw new IllegalStateException("Укажите файл модели");
    }

    private void copyModelFile(Path sourceModelPath, Path targetModelPath) {
        if (sourceModelPath == null || sourceModelPath.equals(targetModelPath)) {
            return;
        }

        FileSystemUtility.copyFile(sourceModelPath, targetModelPath, "Не удалось скопировать файл модели");
    }

    private void deleteObsoleteFiles(
            Path targetIntegrationPath,
            Path targetModelPath,
            Path targetConfigPath,
            ModelIntegrationDescriptor existing
    ) {
        if (existing == null) {
            return;
        }

        if (!existing.integrationPath().equals(targetIntegrationPath)) {
            FileSystemUtility.deleteObsoleteDirectory(
                    existing.integrationPath(),
                    targetIntegrationPath,
                    "Не удалось удалить " + existing.integrationPath().getFileName()
            );
            return;
        }

        if (!existing.modelPath().equals(targetModelPath)) {
            FileSystemUtility.deleteIfExists(existing.modelPath(), "Не удалось удалить " + existing.modelPath().getFileName());
        }
        if (!existing.configPath().equals(targetConfigPath)) {
            FileSystemUtility.deleteIfExists(existing.configPath(), "Не удалось удалить " + existing.configPath().getFileName());
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value;
    }

}
