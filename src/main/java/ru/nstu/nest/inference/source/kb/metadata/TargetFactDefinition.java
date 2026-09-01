package ru.nstu.nest.inference.source.kb.metadata;

import ru.nstu.nest.inference.dto.Address;

import java.util.Objects;

public record TargetFactDefinition(
        Address address
) {

    public TargetFactDefinition {
        Objects.requireNonNull(address, "Адрес целевого факта не должен быть пустым");
    }

    public TargetFactDefinition(String address) {
        this(Address.parse(address));
    }

    public String object() {
        return address.object();
    }

    public String attribute() {
        return address.attribute();
    }

    public String displayName() {
        return address.value();
    }

}
