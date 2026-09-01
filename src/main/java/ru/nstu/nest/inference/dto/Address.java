package ru.nstu.nest.inference.dto;

import java.util.Locale;

public record Address(
        String object,
        String attribute
) {

    public Address {
        if (object == null || object.isBlank()) {
            throw new IllegalArgumentException("Объект адреса не должен быть пустым");
        }

        object = object.trim();
        attribute = attribute == null || attribute.isBlank()
                ? null
                : attribute.trim();
    }

    public static Address of(String object, String attribute) {
        return new Address(object, attribute);
    }

    public static Address parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Адрес не должен быть пустым");
        }

        int separatorIndex = value.indexOf('.');
        if (separatorIndex < 0) {
            return new Address(value, null);
        }

        String object = value.substring(0, separatorIndex);
        String attribute = value.substring(separatorIndex + 1);
        return new Address(object, attribute);
    }

    public String value() {
        return attribute == null ? object : object + "." + attribute;
    }

    public String key() {
        return value().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value();
    }

}
