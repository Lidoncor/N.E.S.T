package ru.nstu.nest.runtime.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;
import ru.nstu.nest.files.service.project.ProjectSettingsService;
import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.context.InferenceEngine;
import ru.nstu.nest.inference.service.InferenceResultResolver;
import ru.nstu.nest.files.service.project.ProjectStructure;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InferenceEngineFactory {

    private final InferenceResultResolver inferenceResultResolver;
    private final ModelInferenceAgentFactory modelInferenceAgentFactory;
    private final KnowledgebaseInferenceAgentFactory knowledgebaseInferenceAgentFactory;
    private final ProjectSettingsService projectSettingsService;

    public InferenceEngine create(ProjectStructure projectStructure) {
        List<InferenceAgent> projectAgents = new ArrayList<>();
        projectAgents.addAll(knowledgebaseInferenceAgentFactory.create(projectStructure.knowledgebasePath()));
        projectAgents.addAll(modelInferenceAgentFactory.create(projectStructure.rootPath(), projectStructure.modelPath()));

        if (projectAgents.isEmpty()) {
            throw new IllegalStateException("Project does not contain inference agents");
        }

        ProjectSettingsProperties settings = projectSettingsService.load(projectStructure);
        return new InferenceEngine(projectAgents, inferenceResultResolver, settings.getInferenceCycleLimit());
    }

}

