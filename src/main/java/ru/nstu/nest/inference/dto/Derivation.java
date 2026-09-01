package ru.nstu.nest.inference.dto;

import java.util.List;
import java.util.Objects;

public record Derivation(
        DerivationSource source,
        List<Fact> supportingFacts
) {

    public Derivation {
        Objects.requireNonNull(source, "Источник вывода не должен быть пустым");
        Objects.requireNonNull(supportingFacts, "Поддерживающие факты вывода не должны быть пустыми");

        supportingFacts = List.copyOf(supportingFacts);
    }

}
