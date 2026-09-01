package ru.nstu.nest.inference.source.kb.metadata;

import java.util.List;
import java.util.Objects;

public record FrameDefinition(
        String name,
        List<SlotDefinition> slots
) {

    public FrameDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя фрейма не должно быть пустым");
        }

        Objects.requireNonNull(slots, "Слоты фрейма не должны быть пустыми");

        name = name.trim();
        slots = List.copyOf(slots);
    }

}
