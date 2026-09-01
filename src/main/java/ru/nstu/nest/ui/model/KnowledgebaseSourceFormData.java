package ru.nstu.nest.ui.model;

import ru.nstu.nest.files.properties.kb.KnowledgebaseAgentType;

import java.nio.file.Path;

public record KnowledgebaseSourceFormData(
        String sourceName,
        Path sourceRulesPath,
        KnowledgebaseAgentType agentType
) {
}
