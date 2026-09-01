package ru.nstu.nest.inference.agent.impl;

import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.InputVector;
import ru.nstu.nest.inference.mapper.ModelInputMapper;
import ru.nstu.nest.inference.mapper.OutputFactMapper;
import ru.nstu.nest.inference.source.model.OnnxClassificationModel;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ModelInferenceAgent implements InferenceAgent, AutoCloseable {

    private final String sourceId;
    private final Path projectRoot;
    private final ModelSourceProperties properties;
    private final ModelInputMapper inputMapper;
    private final OutputFactMapper outputFactMapper;
    private final OnnxClassificationModel onnxModel;
    private final Set<String> triggeringFacts;

    public ModelInferenceAgent(
            String sourceId,
            Path projectRoot,
            ModelSourceProperties properties,
            ModelInputMapper inputMapper,
            OutputFactMapper outputFactMapper,
            OnnxClassificationModel onnxModel
    ) {
        this.sourceId = sourceId;
        this.projectRoot = projectRoot;
        this.properties = properties;
        this.inputMapper = inputMapper;
        this.outputFactMapper = outputFactMapper;
        this.onnxModel = onnxModel;
        this.triggeringFacts = resolveTriggeringFacts(properties);
    }

    @Override
    public boolean supports(List<Fact> incomingFacts) {
        return incomingFacts.stream()
                .map(fact -> fact.address().value())
                .anyMatch(triggeringFacts::contains);
    }

    @Override
    public List<Fact> execute(WorkingMemory workingMemory, List<Fact> incomingFacts) {
        if (!inputMapper.hasRequiredFacts(workingMemory, properties.getInput())) {
            return List.of();
        }

        InputVector inputVector = inputMapper.map(workingMemory, properties.getInput(), projectRoot);
        float[] rawOutput = onnxModel.run(inputVector);
        return outputFactMapper.map(sourceId, rawOutput, properties, inputVector.supportingFacts());
    }

    @Override
    public void close() {
        onnxModel.close();
    }

    private Set<String> resolveTriggeringFacts(ModelSourceProperties properties) {
        List<String> configuredTriggers = properties.getTrigger().getFacts();
        if (configuredTriggers != null && !configuredTriggers.isEmpty()) {
            return Set.copyOf(configuredTriggers);
        }
        return inputMapper.fallbackTriggeringFacts(properties.getInput());
    }

}
