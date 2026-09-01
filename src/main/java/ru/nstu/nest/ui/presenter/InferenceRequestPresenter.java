package ru.nstu.nest.ui.presenter;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.AttachmentDescriptor;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.service.model.ModelIntegrationReadService;
import ru.nstu.nest.files.service.attachment.AttachmentReference;
import ru.nstu.nest.files.service.attachment.AttachmentService;
import ru.nstu.nest.files.service.project.ActiveProject;
import ru.nstu.nest.files.service.project.ProjectStructure;
import ru.nstu.nest.inference.context.InferenceEngine;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.service.TargetFactResolver;
import ru.nstu.nest.inference.source.kb.metadata.KnowledgebaseMetadata;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;
import ru.nstu.nest.runtime.InferenceEngineRegistry;
import ru.nstu.nest.ui.model.InferenceRequestHistoryEntry;
import ru.nstu.nest.ui.model.KnowledgebaseSourceInputStatus;
import ru.nstu.nest.ui.presenter.actions.InferenceRequestPanelActions;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.attachment.AttachmentPreviewDialog;
import ru.nstu.nest.ui.view.panel.InferenceRequestPanel;
import ru.nstu.nest.ui.view.panel.KnowledgebaseSourcePanel;

import javax.swing.SwingWorker;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
public class InferenceRequestPresenter {

    private static final String PROJECT_NOT_RUNNING_ERROR = "Сначала запустите проект";
    private static final String REQUEST_FAILED_ERROR = "Не удалось выполнить запрос";

    private final ActiveProject activeProject;
    private final AttachmentService attachmentService;
    private final ModelIntegrationReadService modelIntegrationReadService;
    private final InferenceEngineRegistry inferenceEngineRegistry;
    private final TargetFactResolver targetFactResolver;
    private final InferenceRequestPanel inferenceRequestPanel;
    private final KnowledgebaseSourcePanel knowledgebaseSourcePanel;

    private int requestCounter;

    public InferenceRequestPresenter(
            ActiveProject activeProject,
            AttachmentService attachmentService,
            ModelIntegrationReadService modelIntegrationReadService,
            InferenceEngineRegistry inferenceEngineRegistry,
            TargetFactResolver targetFactResolver,
            InferenceRequestPanel inferenceRequestPanel,
            KnowledgebaseSourcePanel knowledgebaseSourcePanel
    ) {
        this.activeProject = activeProject;
        this.attachmentService = attachmentService;
        this.modelIntegrationReadService = modelIntegrationReadService;
        this.inferenceEngineRegistry = inferenceEngineRegistry;
        this.targetFactResolver = targetFactResolver;
        this.inferenceRequestPanel = inferenceRequestPanel;
        this.knowledgebaseSourcePanel = knowledgebaseSourcePanel;

        bind();
    }

    public void projectStarted(ProjectStructure projectStructure, List<KnowledgebaseMetadata> metadata) {
        inferenceRequestPanel.setMetadata(
                metadata,
                imageAttachmentFactAddresses(projectStructure),
                attachmentService.list(projectStructure)
        );
    }

    public void projectStopped() {
        inferenceRequestPanel.setProjectStopped();
        knowledgebaseSourcePanel.clearSourceStatuses();
    }

    private void bind() {
        inferenceRequestPanel.bind(new InferenceRequestPanelActions() {
            @Override
            public void submitRequested(List<Fact> inputFacts, List<TargetFactDefinition> targets) {
                handleSubmit(inputFacts, targets);
            }

            @Override
            public void sourceStatusesChanged(Map<String, KnowledgebaseSourceInputStatus> sourceStatuses) {
                knowledgebaseSourcePanel.setSourceStatuses(sourceStatuses);
            }

            @Override
            public AttachmentDescriptor createImageAttachment(Path sourcePath) {
                return attachmentService.createImageAttachment(activeProject.requireProject(), sourcePath);
            }

            @Override
            public void previewAttachmentRequested(String attachmentValue) {
                showAttachmentPreview(attachmentValue);
            }
        });
    }

    private Set<String> imageAttachmentFactAddresses(ProjectStructure projectStructure) {
        return modelIntegrationReadService.list(projectStructure.modelPath()).stream()
                .map(descriptor -> descriptor.config().getInput())
                .filter(input -> resolveInputType(input) == ModelInputType.IMAGE_FACT)
                .map(ModelSourceProperties.Input::getImage)
                .filter(image -> image != null && image.getFact() != null && !image.getFact().isBlank())
                .map(ModelSourceProperties.Image::getFact)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private ModelInputType resolveInputType(ModelSourceProperties.Input input) {
        if (input == null || input.getType() == null) {
            return ModelInputType.FACT_VECTOR;
        }
        return input.getType();
    }

    private void showAttachmentPreview(String attachmentValue) {
        try {
            ProjectStructure projectStructure = activeProject.requireProject();
            String id = AttachmentReference.requireId(attachmentValue);
            AttachmentDescriptor descriptor = attachmentService.find(projectStructure, id)
                    .orElseThrow(() -> new IllegalArgumentException("Attachment не найден: " + id));
            AttachmentPreviewDialog.show(
                    inferenceRequestPanel,
                    attachmentService.requireAttachmentPath(projectStructure.rootPath(), id),
                    descriptor.name()
            );
        } catch (RuntimeException e) {
            SwingDialogs.showError(inferenceRequestPanel, e.getMessage(), "Attachment");
        }
    }

    private void handleSubmit(List<Fact> inputFacts, List<TargetFactDefinition> targets) {
        ProjectStructure projectStructure;
        InferenceEngine engine;
        try {
            projectStructure = activeProject.requireProject();
            engine = inferenceEngineRegistry.getEngine(projectStructure.name())
                    .orElseThrow(() -> new IllegalStateException(PROJECT_NOT_RUNNING_ERROR));
        } catch (RuntimeException e) {
            inferenceRequestPanel.showRequestError(e.getMessage());
            return;
        }

        inferenceRequestPanel.setRequestRunning(true);

        SwingWorker<InferenceRequestHistoryEntry, Void> worker = new SwingWorker<>() {
            @Override
            protected InferenceRequestHistoryEntry doInBackground() {
                List<Fact> responseFacts = engine.startInference(inputFacts);
                var targetResolutions = targetFactResolver.resolve(responseFacts, targets);
                return new InferenceRequestHistoryEntry(
                        requestCounter + 1,
                        LocalDateTime.now(),
                        inputFacts,
                        targets,
                        targetResolutions,
                        responseFacts
                );
            }

            @Override
            protected void done() {
                inferenceRequestPanel.setRequestRunning(false);
                try {
                    InferenceRequestHistoryEntry entry = get();
                    requestCounter = entry.number();
                    inferenceRequestPanel.addHistoryEntry(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    inferenceRequestPanel.showRequestError(REQUEST_FAILED_ERROR);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    inferenceRequestPanel.showRequestError(requestErrorMessage(cause));
                }
            }
        };

        worker.execute();
    }

    private String requestErrorMessage(Throwable cause) {
        if (cause == null) {
            return REQUEST_FAILED_ERROR;
        }

        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            return REQUEST_FAILED_ERROR + ": " + cause.getClass().getSimpleName();
        }
        return message;
    }

}
