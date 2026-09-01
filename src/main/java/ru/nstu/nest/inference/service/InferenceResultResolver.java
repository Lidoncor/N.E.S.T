package ru.nstu.nest.inference.service;

import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Fact;

import java.util.List;

public interface InferenceResultResolver {

    List<Fact> resolve(WorkingMemory workingMemory, List<Fact> inputFacts);

}
