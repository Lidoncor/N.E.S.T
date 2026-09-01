package ru.nstu.nest.runtime;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.context.InferenceEngine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InferenceEngineRegistry {

    private final Map<String, InferenceEngine> activeEngines = new ConcurrentHashMap<>();

    public void register(String projectId, InferenceEngine engine) {
        InferenceEngine previous = activeEngines.put(projectId, engine);

        closeEngine(previous);
    }

    public void unregister(String projectId) {
        InferenceEngine removed = activeEngines.remove(projectId);

        closeEngine(removed);
    }

    public Optional<InferenceEngine> getEngine(String projectId) {
        return Optional.ofNullable(activeEngines.get(projectId));
    }

    private void closeEngine(InferenceEngine engine) {
        if (engine == null) {
            return;
        }
        engine.close();
    }

}
