package ru.nstu.nest.inference.service.impl;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Derivation;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.service.InferenceResultResolver;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.newSetFromMap;

@Service
public class InferenceResultResolverImpl implements InferenceResultResolver {

    @Override
    public List<Fact> resolve(WorkingMemory workingMemory, List<Fact> inputFacts) {
        Set<String> inputKeys = inputFacts.stream()
                .map(Fact::hypothesisKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Derivation> usedDerivations = collectUsedDerivations(workingMemory);

        return workingMemory.facts().stream()
                .map(fact -> toTerminalFact(fact, inputKeys, usedDerivations))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private Set<Derivation> collectUsedDerivations(WorkingMemory workingMemory) {
        Set<Derivation> usedDerivations = newSetFromMap(new IdentityHashMap<>());

        workingMemory.facts().stream()
                .flatMap(fact -> fact.derivationHistory().stream())
                .flatMap(derivation -> derivation.supportingFacts().stream())
                .flatMap(fact -> fact.derivationHistory().stream())
                .forEach(usedDerivations::add);

        return usedDerivations;
    }

    private Optional<Fact> toTerminalFact(
            Fact fact,
            Set<String> inputKeys,
            Set<Derivation> usedDerivations
    ) {
        if (fact.derivationHistory().isEmpty() || inputKeys.contains(fact.hypothesisKey())) {
            return Optional.empty();
        }

        List<Derivation> terminalDerivations = new ArrayList<>();
        for (Derivation derivation : fact.derivationHistory()) {
            if (!usedDerivations.contains(derivation)) {
                terminalDerivations.add(derivation);
            }
        }

        if (terminalDerivations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Fact(fact.address(), fact.value(), terminalDerivations));
    }
}
