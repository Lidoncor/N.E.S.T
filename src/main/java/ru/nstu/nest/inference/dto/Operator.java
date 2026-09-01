package ru.nstu.nest.inference.dto;

public enum Operator {

    EQ, GT, LT, NE;

    public static Operator fromString(String op) {
        return switch (op.toUpperCase()) {
            case "=", "EQ" -> EQ;
            case ">", "GT" -> GT;
            case "<", "LT" -> LT;
            case "!=", "NE" -> NE;
            default -> throw new UnsupportedOperationException();
        };
    }

}