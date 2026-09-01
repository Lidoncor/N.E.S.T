package ru.nstu.nest.runtime.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.files.properties.kb.KnowledgebaseAgentType;
import ru.nstu.nest.files.service.kb.KnowledgebaseSourceReadService;
import ru.nstu.nest.inference.agent.InferenceAgent;
import ru.nstu.nest.inference.agent.impl.ForwardChainInferenceAgent;
import ru.nstu.nest.inference.mapper.KnowledgeBaseFactMapper;
import ru.nstu.nest.inference.source.kb.KlbFileReader;
import ru.nstu.nest.inference.source.kb.KlbParser;
import ru.nstu.nest.inference.source.kb.KnowledgeBase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgebaseInferenceAgentFactory {

    private final KlbParser klbParser;
    private final KlbFileReader klbFileReader;
    private final KnowledgeBaseFactMapper factMapper;
    private final KnowledgebaseSourceReadService sourceReadService;

    public List<InferenceAgent> create(Path knowledgebaseDirectory) {
        List<KnowledgebaseSourceDescriptor> sources = sourceReadService.list(knowledgebaseDirectory);

        List<InferenceAgent> agents = new ArrayList<>(sources.size());
        for (KnowledgebaseSourceDescriptor source : sources) {
            agents.add(createAgent(source));
        }

        return agents;
    }

    private InferenceAgent createAgent(KnowledgebaseSourceDescriptor source) {
        KnowledgeBase knowledgeBase = parseKnowledgeBase(source);

        if (source.config().getAgentType() == KnowledgebaseAgentType.FORWARD_CHAIN) {
            return new ForwardChainInferenceAgent(knowledgeBase, factMapper);
        }

        throw new IllegalArgumentException("Unsupported knowledgebase agent type: " + source.config().getAgentType());
    }

    private KnowledgeBase parseKnowledgeBase(KnowledgebaseSourceDescriptor source) {
        try {
            return klbParser.createKnowledgeBase(
                    klbFileReader.read(source.rulesPath()),
                    source.name()
            );
        } catch (RuntimeException e) {
            throw new IllegalStateException("Knowledgebase " + source.name() + ": " + e.getMessage(), e);
        }
    }

}
