package ru.nstu.nest.files.service.kb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.inference.source.kb.KlbFileReader;
import ru.nstu.nest.inference.source.kb.KlbParser;
import ru.nstu.nest.inference.source.kb.metadata.KnowledgebaseMetadata;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgebaseMetadataReadService {

    private final KnowledgebaseSourceReadService sourceReadService;
    private final KlbFileReader klbFileReader;
    private final KlbParser klbParser;

    public List<KnowledgebaseMetadata> load(Path knowledgebaseDirectory) {
        return sourceReadService.list(knowledgebaseDirectory).stream()
                .map(this::load)
                .toList();
    }

    private KnowledgebaseMetadata load(KnowledgebaseSourceDescriptor source) {
        try {
            return klbParser.parseMetadata(klbFileReader.read(source.rulesPath()), source.name());
        } catch (RuntimeException e) {
            throw new IllegalStateException("База знаний " + source.name() + ": " + e.getMessage(), e);
        }
    }

}
