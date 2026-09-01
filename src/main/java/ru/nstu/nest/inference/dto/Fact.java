package ru.nstu.nest.inference.dto;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record Fact(
        Address address,
        String value,
        List<Derivation> derivationHistory
) {

    public Fact {
        Objects.requireNonNull(address, "Адрес факта не должен быть пустым");
        Objects.requireNonNull(derivationHistory, "История вывода факта не должна быть пустой");

        derivationHistory = List.copyOf(derivationHistory);
    }

    public static Fact of(
            String object,
            String attribute,
            String value
    ) {
        return new Fact(Address.of(object, attribute), value, List.of());
    }

    public String normalizedValue() {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String hypothesisKey() {
        return address().key() + "." + normalizedValue();
    }

}
