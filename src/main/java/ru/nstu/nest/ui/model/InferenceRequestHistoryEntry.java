package ru.nstu.nest.ui.model;

import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.service.TargetFactResolution;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record InferenceRequestHistoryEntry(
        int number,
        LocalDateTime timestamp,
        List<Fact> inputFacts,
        List<TargetFactDefinition> targets,
        List<TargetFactResolution> targetResolutions,
        List<Fact> responseFacts
) {

    public InferenceRequestHistoryEntry {
        Objects.requireNonNull(timestamp, "Время запроса не должно быть пустым");
        Objects.requireNonNull(inputFacts, "Входные факты не должны быть пустыми");
        Objects.requireNonNull(targets, "Целевые факты не должны быть пустыми");
        Objects.requireNonNull(targetResolutions, "Результаты целевых фактов не должны быть пустыми");
        Objects.requireNonNull(responseFacts, "Факты ответа не должны быть пустыми");

        inputFacts = List.copyOf(inputFacts);
        targets = List.copyOf(targets);
        targetResolutions = List.copyOf(targetResolutions);
        responseFacts = List.copyOf(responseFacts);
    }

}
