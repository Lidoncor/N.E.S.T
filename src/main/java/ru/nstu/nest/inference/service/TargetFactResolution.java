package ru.nstu.nest.inference.service;

import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;

import java.util.List;
import java.util.Objects;

public record TargetFactResolution(
        TargetFactDefinition target,
        List<Fact> facts
) {

    public TargetFactResolution {
        Objects.requireNonNull(target, "Целевой факт не должен быть пустым");
        Objects.requireNonNull(facts, "Найденные целевые факты не должны быть пустыми");

        facts = List.copyOf(facts);
    }

    public boolean found() {
        return !facts.isEmpty();
    }

    public List<String> values() {
        return facts.stream()
                .map(Fact::value)
                .toList();
    }

}
