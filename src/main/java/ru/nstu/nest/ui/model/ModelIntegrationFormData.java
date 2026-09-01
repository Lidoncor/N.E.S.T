package ru.nstu.nest.ui.model;

import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.TaskType;

import java.nio.file.Path;
import java.util.List;

public record ModelIntegrationFormData(
        Path sourceModelPath,
        TaskType taskType,
        List<String> triggerFacts,
        String inputTensorName,
        ModelInputType inputType,
        String outputTensorName,
        List<ModelSourceProperties.Feature> inputFeatures,
        ModelSourceProperties.Image imageInput,
        List<ModelSourceProperties.ClassMapping> outputClassMapping,
        String resultFact
) {
}
