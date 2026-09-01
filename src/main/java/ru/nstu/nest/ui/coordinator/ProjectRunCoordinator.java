package ru.nstu.nest.ui.coordinator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;
import ru.nstu.nest.files.service.kb.KnowledgebaseMetadataReadService;
import ru.nstu.nest.files.service.project.ProjectSettingsService;
import ru.nstu.nest.runtime.factory.InferenceEngineFactory;
import ru.nstu.nest.runtime.InferenceEngineRegistry;
import ru.nstu.nest.files.service.project.ActiveProject;
import ru.nstu.nest.files.service.project.ProjectStructure;
import ru.nstu.nest.inference.source.kb.metadata.KnowledgebaseMetadata;
import ru.nstu.nest.ui.presenter.InferenceRequestPresenter;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.common.ProgressDialog;
import ru.nstu.nest.ui.view.dialog.project.ProjectSettingsDialog;
import ru.nstu.nest.ui.view.panel.ProjectRunStatusPanel;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class ProjectRunCoordinator {

    private final ActiveProject activeProject;
    private final InferenceEngineRegistry inferenceEngineRegistry;
    private final InferenceEngineFactory inferenceEngineFactory;
    private final KnowledgebaseMetadataReadService knowledgebaseMetadataReadService;
    private final InferenceRequestPresenter inferenceRequestPresenter;
    private final ProjectRunStatusPanel projectRunStatusPanel;
    private final ProjectSettingsService projectSettingsService;
    private final ProjectSettingsDialog projectSettingsDialog;

    public void runProject(JFrame parent) {
        ProjectStructure projectStructure = activeProject.requireProject();
        ProgressDialog dialog = new ProgressDialog(parent, "Инициализация движка вывода...");
        projectRunStatusPanel.setStarting();

        SwingWorker<List<KnowledgebaseMetadata>, String> worker = new SwingWorker<>() {

            @Override
            protected List<KnowledgebaseMetadata> doInBackground() {
                publish("Чтение баз знаний...");
                var engine = inferenceEngineFactory.create(projectStructure);

                publish("Загрузка входных фреймов...");
                var metadata = knowledgebaseMetadataReadService.load(projectStructure.knowledgebasePath());

                publish("Регистрация движка вывода...");
                inferenceEngineRegistry.register(projectStructure.name(), engine);

                return metadata;
            }

            @Override
            protected void process(List<String> chunks) {
                dialog.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    var metadata = get();
                    projectRunStatusPanel.setRunning();
                    inferenceRequestPresenter.projectStarted(projectStructure, metadata);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    projectRunStatusPanel.setProjectReady();
                    showRunError(parent, "Запуск прерван");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    projectRunStatusPanel.setProjectReady();
                    showRunError(parent, cause != null ? cause.getMessage() : "Не удалось запустить проект");
                }
                dialog.dispose();
            }
        };

        worker.execute();
        dialog.setVisible(true);
    }

    public void stopProject() {
        ProjectStructure projectStructure = activeProject.requireProject();
        inferenceEngineRegistry.unregister(projectStructure.name());
        inferenceRequestPresenter.projectStopped();
        projectRunStatusPanel.setProjectReady();
    }

    public void showSettings(JFrame parent) {
        ProjectStructure projectStructure = activeProject.requireProject();
        ProjectSettingsProperties settings = projectSettingsService.load(projectStructure);
        var formData = projectSettingsDialog.showDialog(parent, settings);
        if (formData == null) {
            return;
        }

        ProjectSettingsProperties updatedSettings = new ProjectSettingsProperties();
        updatedSettings.setInferenceCycleLimit(formData.inferenceCycleLimit());
        projectSettingsService.save(projectStructure, updatedSettings);
    }

    private void showRunError(JFrame parent, String message) {
        SwingDialogs.showError(parent, message, "Ошибка запуска");
    }
}
