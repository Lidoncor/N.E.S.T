package ru.nstu.nest.ui.service;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.source.kb.metadata.FrameDefinition;

import java.util.List;
import java.util.Map;

@Service
public class FrameFactMapper {

    public List<Fact> toFacts(FrameDefinition frame, Map<String, String> valuesBySlotName) {
        return frame.slots().stream()
                .map(slot -> Fact.of(
                        frame.name(),
                        slot.name(),
                        valuesBySlotName.getOrDefault(slot.name(), "").trim()
                ))
                .toList();
    }

}
