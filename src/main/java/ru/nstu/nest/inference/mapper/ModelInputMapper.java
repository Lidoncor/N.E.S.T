package ru.nstu.nest.inference.mapper;

import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.InputVector;

import java.nio.file.Path;
import java.util.Set;

public interface ModelInputMapper {

    ModelInputType inputType();

    boolean hasRequiredFacts(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig);

    InputVector map(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig, Path projectRoot);

    Set<String> fallbackTriggeringFacts(ModelSourceProperties.Input inputConfig);
}
