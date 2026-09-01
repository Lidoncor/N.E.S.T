package ru.nstu.nest.inference.source.kb.metadata;

import java.util.List;
import java.util.Objects;

public record SlotDefinition(
        String name,
        String question,
        boolean numeric,
        List<String> options
) {

    public SlotDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя слота не должно быть пустым");
        }

        Objects.requireNonNull(options, "Варианты слота не должны быть пустыми");

        name = name.trim();
        question = question == null || question.isBlank() ? null : question.trim();
        options = options.stream()
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .toList();
    }

    public String label() {
        return question == null ? name : question;
    }

}
