package ru.nstu.nest.inference.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.nstu.nest.inference.dto.Action;
import ru.nstu.nest.inference.dto.Derivation;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.KnowledgeBaseDerivationSource;
import ru.nstu.nest.inference.dto.Rule;
import ru.nstu.nest.inference.source.kb.KnowledgeBase;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class KnowledgeBaseFactMapper {

    public Fact toFact(KnowledgeBase knowledgeBase, Rule rule, List<Fact> supportingFacts) {
        return map(
                rule.actions().getFirst(),
                getDerivations(knowledgeBase, rule, supportingFacts)
        );
    }

    @Mapping(target = "address", source = "action.address")
    @Mapping(target = "value", source = "action.value")
    @Mapping(target = "derivationHistory", source = "derivations")
    protected abstract Fact map(Action action, List<Derivation> derivations);

    protected List<Derivation> getDerivations(
            KnowledgeBase knowledgeBase,
            Rule rule,
            List<Fact> supportingFacts
    ) {
        return List.of(
                new Derivation(
                        new KnowledgeBaseDerivationSource(knowledgeBase.sourceId(), rule.id()),
                        supportingFacts
                )
        );
    }

}
