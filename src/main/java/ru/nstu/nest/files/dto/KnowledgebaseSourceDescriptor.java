package ru.nstu.nest.files.dto;

import ru.nstu.nest.files.properties.kb.KnowledgebaseSourceProperties;

import java.nio.file.Path;

public record KnowledgebaseSourceDescriptor(
        String name,
        Path integrationPath,
        Path rulesPath,
        Path configPath,
        KnowledgebaseSourceProperties config
) {
}
