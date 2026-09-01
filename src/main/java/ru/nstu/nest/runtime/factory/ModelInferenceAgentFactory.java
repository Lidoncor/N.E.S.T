package ru.nstu.nest.runtime.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.service.model.ModelIntegrationReadService;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.properties.validator.ModelSourcePropertiesValidator;
import ru.nstu.nest.files.properties.model.TaskType;
import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.agent.impl.ModelInferenceAgent;
import ru.nstu.nest.inference.mapper.ModelInputMapper;
import ru.nstu.nest.inference.mapper.OutputFactMapper;
import ru.nstu.nest.inference.mapper.ClassificationOutputFactMapper;
import ru.nstu.nest.inference.source.model.OnnxClassificationModelFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModelInferenceAgentFactory {

    private final ModelSourcePropertiesValidator propertiesValidator;
    private final List<ModelInputMapper> inputMappers;
    private final ClassificationOutputFactMapper classificationOutputFactMapper;
    private final OnnxClassificationModelFactory onnxClassificationModelFactory;
    private final ModelIntegrationReadService integrationReadService;

    public List<InferenceAgent> create(Path projectRoot, Path modelDirectory) {
        List<ModelIntegrationDescriptor> integrations = integrationReadService.list(modelDirectory);

        List<InferenceAgent> agents = new ArrayList<>(integrations.size());
        for (ModelIntegrationDescriptor integration : integrations) {
            agents.add(createAgent(projectRoot, integration));
        }

        return agents;
    }

    private ModelInferenceAgent createAgent(Path projectRoot, ModelIntegrationDescriptor integration) {
        ModelSourceProperties properties = integration.config();
        propertiesValidator.validate(properties);

        ModelInputMapper inputMapper = resolveInputMapper(properties);
        OutputFactMapper outputFactMapper = resolveOutputFactMapper(properties);

        return new ModelInferenceAgent(
                integration.name(),
                projectRoot,
                properties,
                inputMapper,
                outputFactMapper,
                onnxClassificationModelFactory.create(integration.modelPath(), properties)
        );
    }

    private ModelInputMapper resolveInputMapper(ModelSourceProperties properties) {
        ModelInputType inputType = properties.getInput().getType() == null
                ? ModelInputType.FACT_VECTOR
                : properties.getInput().getType();
        return inputMappers.stream()
                .filter(mapper -> mapper.inputType() == inputType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported model input type: " + inputType));
    }

    private OutputFactMapper resolveOutputFactMapper(ModelSourceProperties properties) {
        if (properties.getModel().getTaskType() == TaskType.CLASSIFICATION) {
            return classificationOutputFactMapper;
        }

        throw new IllegalArgumentException("Unsupported task type: " + properties.getModel().getTaskType());
    }

}
