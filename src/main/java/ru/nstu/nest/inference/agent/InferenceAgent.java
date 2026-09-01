package ru.nstu.nest.inference.agent;

import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Fact;

import java.util.List;

public interface InferenceAgent {

    boolean supports(List<Fact> incomingFacts);

    List<Fact> execute(WorkingMemory workingMemory, List<Fact> incomingFacts);

}
