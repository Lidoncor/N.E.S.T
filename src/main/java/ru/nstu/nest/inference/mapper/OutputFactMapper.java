package ru.nstu.nest.inference.mapper;

import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.inference.dto.Fact;

import java.util.List;

public interface OutputFactMapper {

    List<Fact> map(String sourceId, float[] rawOutput, ModelSourceProperties properties, List<Fact> supportingFacts);

}
