package ru.nstu.nest.inference.mapper;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.InputVector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InputFactMapper implements ModelInputMapper {

    @Override
    public ModelInputType inputType() {
        return ModelInputType.FACT_VECTOR;
    }

    @Override
    public boolean hasRequiredFacts(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig) {
        return orderedFeatures(inputConfig).stream()
                .map(ModelSourceProperties.Feature::getFact)
                .allMatch(workingMemory::has);
    }

    public InputVector map(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig) {
        return map(workingMemory, inputConfig, null);
    }

    @Override
    public InputVector map(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig, Path projectRoot) {
        List<ModelSourceProperties.Feature> features = orderedFeatures(inputConfig);
        List<Fact> supportingFacts = new ArrayList<>(features.size());
        float[] values = new float[features.size()];

        for (ModelSourceProperties.Feature feature : features) {
            Fact fact = selectFact(workingMemory, feature.getFact());
            supportingFacts.add(fact);
            values[feature.getIndex()] = parseFloatValue(fact);
        }

        return new InputVector(List.copyOf(supportingFacts), values);
    }

    @Override
    public Set<String> fallbackTriggeringFacts(ModelSourceProperties.Input inputConfig) {
        return orderedFeatures(inputConfig).stream()
                .map(ModelSourceProperties.Feature::getFact)
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<ModelSourceProperties.Feature> orderedFeatures(ModelSourceProperties.Input inputConfig) {
        return inputConfig.getFeatures().stream()
                .sorted(Comparator.comparingInt(ModelSourceProperties.Feature::getIndex))
                .toList();
    }

    private Fact selectFact(WorkingMemory workingMemory, String address) {
        List<Fact> candidates = workingMemory.find(address);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Отсутствует обязательный факт: " + address);
        }
        if (candidates.size() > 1) {
            throw new IllegalArgumentException("Входной факт неоднозначен для входа модели: " + address);
        }
        return candidates.getFirst();
    }

    private float parseFloatValue(Fact fact) {
        try {
            return Float.parseFloat(fact.value());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Значение факта для входа модели должно быть числом: " + fact.address().value(),
                    e
            );
        }
    }

}
