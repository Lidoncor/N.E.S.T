package ru.nstu.nest.inference.dto;

public sealed interface DerivationSource permits KnowledgeBaseDerivationSource, ModelDerivationSource {

    String id();

}
