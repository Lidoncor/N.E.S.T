package ru.nstu.nest.inference.agent.impl;

import lombok.RequiredArgsConstructor;
import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Condition;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.Rule;
import ru.nstu.nest.inference.mapper.KnowledgeBaseFactMapper;
import ru.nstu.nest.inference.source.kb.KnowledgeBase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ForwardChainInferenceAgent implements InferenceAgent {

    private final KnowledgeBase knowledgeBase;
    private final KnowledgeBaseFactMapper factMapper;

    @Override
    public boolean supports(List<Fact> incomingFacts) {
        return incomingFacts.stream()
                .map(knowledgeBase::getRulesForFact)
                .anyMatch(rules -> !rules.isEmpty());
    }

    @Override
    public List<Fact> execute(WorkingMemory workingMemory, List<Fact> incomingFacts) {
        Set<Rule> relevantRules = incomingFacts.stream()
                .map(knowledgeBase::getRulesForFact)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Fact> derivedFacts = new ArrayList<>();
        for (Rule rule : relevantRules) {
            deriveFact(rule, workingMemory)
                    .ifPresent(derivedFacts::add);
        }

        return derivedFacts;
    }

    private Optional<Fact> deriveFact(Rule rule, WorkingMemory workingMemory) {
        return resolveBasis(rule, workingMemory)
                .map(supportingFacts -> factMapper.toFact(knowledgeBase, rule, supportingFacts));
    }

    private Optional<List<Fact>> resolveBasis(Rule rule, WorkingMemory workingMemory) {
        List<Fact> supportingFacts = new ArrayList<>();

        for (List<Condition> conditions : conditionsByAddress(rule).values()) {
            List<Fact> matchingFacts = findMatchingFacts(conditions, workingMemory);
            if (matchingFacts.isEmpty()) {
                return Optional.empty();
            }

            supportingFacts.addAll(matchingFacts);
        }

        return Optional.of(supportingFacts);
    }

    private Map<Address, List<Condition>> conditionsByAddress(Rule rule) {
        return rule.conditions().stream()
                .collect(Collectors.groupingBy(
                        Condition::address,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<Fact> findMatchingFacts(List<Condition> conditions, WorkingMemory workingMemory) {
        Address address = conditions.getFirst().address();
        return workingMemory.find(address).stream()
                .filter(fact -> matchesAllConditions(fact, conditions))
                .toList();
    }

    private boolean matchesAllConditions(Fact fact, List<Condition> conditions) {
        return conditions.stream()
                .allMatch(condition -> condition.check(fact));
    }

}
