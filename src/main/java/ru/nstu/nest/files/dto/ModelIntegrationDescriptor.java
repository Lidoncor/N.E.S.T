package ru.nstu.nest.files.dto;

import ru.nstu.nest.files.properties.model.ModelSourceProperties;

import java.nio.file.Path;

public record ModelIntegrationDescriptor(
        String name,
        Path integrationPath,
        Path modelPath,
        Path configPath,
        ModelSourceProperties config
) {
}
