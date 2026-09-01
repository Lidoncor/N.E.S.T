package ru.nstu.nest.inference.service;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TargetFactResolver {

    public List<TargetFactResolution> resolve(
            List<Fact> responseFacts,
            List<TargetFactDefinition> targets
    ) {
        return targets.stream()
                .map(target -> new TargetFactResolution(target, find(responseFacts, target)))
                .toList();
    }

    private List<Fact> find(List<Fact> responseFacts, TargetFactDefinition target) {
        Map<String, Fact> result = new LinkedHashMap<>();
        for (Fact fact : responseFacts) {
            collect(fact, target, result);
        }
        return result.values().stream().toList();
    }

    private void collect(Fact fact, TargetFactDefinition target, Map<String, Fact> result) {
        if (matches(fact.address(), target.address())) {
            result.putIfAbsent(fact.hypothesisKey(), fact);
        }

        fact.derivationHistory().stream()
                .flatMap(derivation -> derivation.supportingFacts().stream())
                .forEach(supportingFact -> collect(supportingFact, target, result));
    }

    private boolean matches(Address factAddress, Address targetAddress) {
        return factAddress.key().equals(targetAddress.key());
    }

}
