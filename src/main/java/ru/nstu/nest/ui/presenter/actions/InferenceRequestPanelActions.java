package ru.nstu.nest.ui.presenter.actions;

import ru.nstu.nest.files.dto.AttachmentDescriptor;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;
import ru.nstu.nest.ui.model.KnowledgebaseSourceInputStatus;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface InferenceRequestPanelActions {

    void submitRequested(List<Fact> inputFacts, List<TargetFactDefinition> targets);

    void sourceStatusesChanged(Map<String, KnowledgebaseSourceInputStatus> sourceStatuses);

    AttachmentDescriptor createImageAttachment(Path sourcePath);

    void previewAttachmentRequested(String attachmentValue);

}
