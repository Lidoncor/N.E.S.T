package ru.nstu.nest.inference.source.kb;

import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.Operator;
import ru.nstu.nest.inference.dto.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class KnowledgeBase {

    private final String sourceId;
    private final Map<String, List<Rule>> preciseIndex = new HashMap<>();
    private final Map<String, List<Rule>> attributeIndex = new HashMap<>();

    public KnowledgeBase(List<Rule> allRules) {
        this("", allRules);
    }

    public KnowledgeBase(String sourceId, List<Rule> allRules) {
        this.sourceId = sourceId;
        for (Rule rule : allRules) {
            index(rule);
        }
    }

    public String sourceId() {
        return sourceId;
    }

    public Set<Rule> getRulesForFact(Fact fact) {
        Set<Rule> result = new LinkedHashSet<>();
        result.addAll(preciseIndex.getOrDefault(fact.hypothesisKey(), Collections.emptyList()));
        result.addAll(attributeIndex.getOrDefault(fact.address().key(), Collections.emptyList()));
        return result;
    }

    private void index(Rule rule) {
        for (var condition : rule.conditions()) {
            add(attributeIndex, condition.address().key(), rule);
            if (condition.operator() == Operator.EQ) {
                add(preciseIndex, hypothesisKey(condition.address(), condition.value()), rule);
            }
        }
    }

    private void add(Map<String, List<Rule>> index, String key, Rule rule) {
        index.computeIfAbsent(key, _ -> new ArrayList<>()).add(rule);
    }

    private String hypothesisKey(Address address, String value) {
        return address.key() + "." + value.trim().toLowerCase(Locale.ROOT);
    }

}
