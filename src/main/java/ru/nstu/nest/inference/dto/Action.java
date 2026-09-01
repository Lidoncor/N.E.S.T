package ru.nstu.nest.inference.dto;

public record Action(
        Address address,
        String value
) {

    public Action(String object, String attribute, String value) {
        this(Address.of(object, attribute), value);
    }

}
