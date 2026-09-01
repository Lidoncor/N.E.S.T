package ru.nstu.nest.inference.context;

import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.service.InferenceResultResolver;

import java.util.ArrayList;
import java.util.List;

public class InferenceEngine implements AutoCloseable {

    private final List<InferenceAgent> inferenceAgents;
    private final InferenceResultResolver resultResolver;
    private final int inferenceCycleLimit;

    public InferenceEngine(
            List<InferenceAgent> inferenceAgents,
            InferenceResultResolver resultResolver,
            int inferenceCycleLimit
    ) {
        this.inferenceAgents = List.copyOf(inferenceAgents);
        this.resultResolver = resultResolver;
        this.inferenceCycleLimit = inferenceCycleLimit;
    }

    public List<Fact> startInference(List<Fact> incomingFacts) {
        List<Fact> initialFacts = List.copyOf(incomingFacts);
        List<Fact> processingQueue = new ArrayList<>(incomingFacts);
        WorkingMemory workingMemory = new WorkingMemory();

        for (int cycles = 0; !processingQueue.isEmpty(); cycles++) {
            ensureCycleLimit(cycles);

            List<Fact> delta = workingMemory.accept(processingQueue);
            if (delta.isEmpty()) {
                break;
            }

            processingQueue = runAgents(workingMemory, delta);
        }

        return resultResolver.resolve(workingMemory, initialFacts);
    }

    private List<Fact> runAgents(WorkingMemory workingMemory, List<Fact> delta) {
        List<Fact> proposals = new ArrayList<>();

        for (InferenceAgent agent : inferenceAgents) {
            if (!agent.supports(delta)) {
                continue;
            }

            proposals.addAll(agent.execute(workingMemory, delta));
        }

        return proposals;
    }

    private void ensureCycleLimit(int cycles) {
        if (cycles >= inferenceCycleLimit) {
            throw new IllegalStateException("Превышен лимит цикла вывода. Возможна циклическая логика.");
        }
    }

    @Override
    public void close() {
        IllegalStateException closeFailure = null;

        for (InferenceAgent agent : inferenceAgents) {
            if (!(agent instanceof AutoCloseable closeable)) {
                continue;
            }

            try {
                closeable.close();
            } catch (Exception e) {
                if (closeFailure == null) {
                    closeFailure = new IllegalStateException("Не удалось закрыть один или несколько агентов вывода", e);
                    continue;
                }
                closeFailure.addSuppressed(e);
            }
        }

        if (closeFailure != null) {
            throw closeFailure;
        }
    }

}
