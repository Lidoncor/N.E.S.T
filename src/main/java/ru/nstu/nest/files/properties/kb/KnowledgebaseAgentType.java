package ru.nstu.nest.files.properties.kb;

public enum KnowledgebaseAgentType {
    FORWARD_CHAIN;

    @Override
    public String toString() {
        return switch (this) {
            case FORWARD_CHAIN -> "Прямой вывод";
        };
    }
}
