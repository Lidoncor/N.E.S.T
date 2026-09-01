package ru.nstu.nest.inference.dto;

public record Condition(
        Address address,
        String value,
        Operator operator
) {

    public Condition(
            String object,
            String attribute,
            String value,
            Operator operator
    ) {
        this(Address.of(object, attribute), value, operator);
    }

    public boolean check(Fact fact) {
        return switch (operator) {
            case EQ -> value.equalsIgnoreCase(fact.value());
            case NE -> !value.equalsIgnoreCase(fact.value());
            case GT -> parseNumber(fact.value()) > parseNumber(value);
            case LT -> parseNumber(fact.value()) < parseNumber(value);
        };
    }

    private double parseNumber(String rawValue) {
        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Численный оператор требует число: " + rawValue, e);
        }
    }

}
