package ru.nstu.nest.inference.source.kb.metadata;

import java.util.List;
import java.util.Objects;

public record KnowledgebaseMetadata(
        String sourceName,
        List<FrameDefinition> inputFrames,
        List<TargetFactDefinition> targetFacts
) {

    public KnowledgebaseMetadata {
        if (sourceName == null) {
            sourceName = "";
        }
        Objects.requireNonNull(inputFrames, "Входные фреймы не должны быть пустыми");
        Objects.requireNonNull(targetFacts, "Целевые факты не должны быть пустыми");

        sourceName = sourceName.trim();
        inputFrames = List.copyOf(inputFrames);
        targetFacts = List.copyOf(targetFacts);
    }

    public static KnowledgebaseMetadata empty(String sourceName) {
        return new KnowledgebaseMetadata(sourceName, List.of(), List.of());
    }

}
