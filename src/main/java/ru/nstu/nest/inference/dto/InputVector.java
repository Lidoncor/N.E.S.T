package ru.nstu.nest.inference.dto;

import java.util.List;

public record InputVector(
        List<Fact> supportingFacts,
        float[] values,
        long[] shape
) {

    public InputVector(List<Fact> supportingFacts, float[] values) {
        this(supportingFacts, values, new long[]{1, values.length});
    }

}
