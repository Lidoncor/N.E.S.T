package ru.nstu.nest.inference.dto;

public record KnowledgeBaseDerivationSource(
        String id,
        String originId
) implements DerivationSource {

}
