package ru.nstu.nest.inference.dto;

import java.util.List;
import java.util.Objects;

public record Rule(
        String id,
        List<Condition> conditions,
        List<Action> actions
) {

    public Rule {
        Objects.requireNonNull(id, "Идентификатор правила не должен быть пустым");
        Objects.requireNonNull(conditions, "Условия правила не должны быть пустыми");
        Objects.requireNonNull(actions, "Действия правила не должны быть пустыми");

        conditions = List.copyOf(conditions);
        actions = List.copyOf(actions);
    }

}
