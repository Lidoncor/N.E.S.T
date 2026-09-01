package ru.nstu.nest.files.properties.model;

public enum TaskType {
    CLASSIFICATION;

    @Override
    public String toString() {
        return switch (this) {
            case CLASSIFICATION -> "Классификация";
        };
    }
}
