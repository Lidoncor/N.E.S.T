package ru.nstu.nest.inference.mapper;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Derivation;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.ModelDerivationSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ClassificationOutputFactMapper implements OutputFactMapper {

    @Override
    public List<Fact> map(String sourceId, float[] rawOutput, ModelSourceProperties properties, List<Fact> supportingFacts) {
        List<ModelSourceProperties.ClassMapping> classMapping = orderedClassMapping(properties);
        if (rawOutput.length != classMapping.size()) {
            throw new IllegalArgumentException(
                    "Размер выходного тензора не совпадает с объявленными классами агента " + sourceId
            );
        }

        List<Fact> classFacts = mapClassFacts(rawOutput, classMapping);
        Fact interpretedFact = mapInterpretedFact(
                sourceId,
                rawOutput,
                properties,
                classMapping,
                classFacts,
                supportingFacts
        );

        List<Fact> mappedFacts = new ArrayList<>(classFacts.size() + 1);
        mappedFacts.addAll(classFacts);
        mappedFacts.add(interpretedFact);
        return List.copyOf(mappedFacts);
    }

    private List<ModelSourceProperties.ClassMapping> orderedClassMapping(ModelSourceProperties properties) {
        return properties.getOutput().getClassMapping().stream()
                .sorted(Comparator.comparingInt(ModelSourceProperties.ClassMapping::getIndex))
                .toList();
    }

    private List<Fact> mapClassFacts(float[] rawOutput, List<ModelSourceProperties.ClassMapping> classMapping) {
        List<Fact> classFacts = new ArrayList<>(classMapping.size());
        for (ModelSourceProperties.ClassMapping mapping : classMapping) {
            classFacts.add(new Fact(
                    Address.parse(mapping.getFact()),
                    Float.toString(rawOutput[mapping.getIndex()]),
                    List.of()
            ));
        }
        return List.copyOf(classFacts);
    }

    private Fact mapInterpretedFact(
            String sourceId,
            float[] rawOutput,
            ModelSourceProperties properties,
            List<ModelSourceProperties.ClassMapping> classMapping,
            List<Fact> classFacts,
            List<Fact> supportingFacts
    ) {
        int winnerIndex = argmax(rawOutput);
        String winnerClass = classMapping.get(winnerIndex).getLabel();
        Address target = Address.parse(properties.getOutput().getResultMapping().getFact());
        Fact winnerFact = classFacts.get(winnerIndex);
        List<Fact> derivationFacts = new ArrayList<>(supportingFacts.size() + 1);
        derivationFacts.addAll(supportingFacts);
        derivationFacts.add(winnerFact);

        return new Fact(
                target,
                winnerClass,
                List.of(new Derivation(new ModelDerivationSource(sourceId), List.copyOf(derivationFacts)))
        );
    }

    private int argmax(float[] values) {
        int winnerIndex = 0;
        float winnerScore = values[0];

        for (int index = 1; index < values.length; index++) {
            if (values[index] <= winnerScore) {
                continue;
            }
            winnerScore = values[index];
            winnerIndex = index;
        }

        return winnerIndex;
    }

}
