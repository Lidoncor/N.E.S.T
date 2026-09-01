package ru.nstu.nest.ui.presenter;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.files.service.kb.KnowledgebaseSourceReadService;
import ru.nstu.nest.files.service.kb.KnowledgebaseSourceWriteService;
import ru.nstu.nest.files.service.project.ActiveProject;
import ru.nstu.nest.files.service.project.ProjectStructure;
import ru.nstu.nest.files.service.utility.KnowledgebaseSourceUtility;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;
import ru.nstu.nest.ui.presenter.actions.KnowledgebaseSourcePanelActions;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.kb.KnowledgebaseSourceFormDialog;
import ru.nstu.nest.ui.view.panel.KnowledgebaseSourcePanel;
import ru.nstu.nest.ui.view.panel.WorkspacePanel;

import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class KnowledgebaseSourcePresenter {

    private final ActiveProject activeProject;
    private final KnowledgebaseSourceReadService readService;
    private final KnowledgebaseSourceWriteService writeService;
    private final KnowledgebaseSourcePanel knowledgebaseSourcePanel;
    private final KnowledgebaseSourceFormDialog knowledgebaseSourceFormDialog;
    private final EditorPanelPresenter editorPanelPresenter;
    private final WorkspacePanel workspacePanel;

    public KnowledgebaseSourcePresenter(
            ActiveProject activeProject,
            KnowledgebaseSourceReadService readService,
            KnowledgebaseSourceWriteService writeService,
            KnowledgebaseSourcePanel knowledgebaseSourcePanel,
            KnowledgebaseSourceFormDialog knowledgebaseSourceFormDialog,
            EditorPanelPresenter editorPanelPresenter,
            WorkspacePanel workspacePanel
    ) {
        this.activeProject = activeProject;
        this.readService = readService;
        this.writeService = writeService;
        this.knowledgebaseSourcePanel = knowledgebaseSourcePanel;
        this.knowledgebaseSourceFormDialog = knowledgebaseSourceFormDialog;
        this.editorPanelPresenter = editorPanelPresenter;
        this.workspacePanel = workspacePanel;

        bind();
    }

    private void bind() {
        knowledgebaseSourcePanel.bind(new KnowledgebaseSourcePanelActions() {
            @Override
            public void create() {
                handleCreateSource();
            }

            @Override
            public void importExisting() {
                handleImportSource();
            }

            @Override
            public void open(KnowledgebaseSourceDescriptor descriptor) {
                handleOpenSource(descriptor);
            }

            @Override
            public void edit(KnowledgebaseSourceDescriptor descriptor) {
                handleEditSource(descriptor);
            }

            @Override
            public void rename(KnowledgebaseSourceDescriptor descriptor) {
                handleRenameSource(descriptor);
            }

            @Override
            public void delete(KnowledgebaseSourceDescriptor descriptor) {
                handleDeleteSource(descriptor);
            }
        });
    }

    public void showProject(ProjectStructure projectStructure) {
        knowledgebaseSourcePanel.showSources(readService.list(projectStructure.knowledgebasePath()));
    }

    private void handleCreateSource() {
        KnowledgebaseSourceFormData formData = knowledgebaseSourceFormDialog.showCreateDialog(knowledgebaseSourcePanel);
        if (formData == null) {
            return;
        }

        runMutation(projectStructure -> writeService.save(projectStructure.knowledgebasePath(), formData, null));
    }

    private void handleImportSource() {
        KnowledgebaseSourceFormData formData = knowledgebaseSourceFormDialog.showImportDialog(knowledgebaseSourcePanel);
        if (formData == null) {
            return;
        }

        runMutation(projectStructure -> writeService.save(projectStructure.knowledgebasePath(), formData, null));
    }

    private void handleOpenSource(KnowledgebaseSourceDescriptor descriptor) {
        editorPanelPresenter.openFile(descriptor.rulesPath().toFile(), descriptor.name());
        workspacePanel.enableEditor();
    }

    private void handleEditSource(KnowledgebaseSourceDescriptor descriptor) {
        KnowledgebaseSourceFormData formData = knowledgebaseSourceFormDialog.showEditDialog(
                knowledgebaseSourcePanel,
                descriptor
        );
        if (formData == null) {
            return;
        }

        runMutation(projectStructure -> writeService.save(projectStructure.knowledgebasePath(), formData, descriptor));
    }

    private void handleDeleteSource(KnowledgebaseSourceDescriptor descriptor) {
        int choice = SwingDialogs.showConfirm(
                knowledgebaseSourcePanel,
                "Удалить базу знаний " + descriptor.name() + "?",
                "Удаление базы знаний",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        runMutation(projectStructure -> {
            writeService.delete(descriptor);
            editorPanelPresenter.discardFile(descriptor.rulesPath().toFile());
        });
    }

    private void handleRenameSource(KnowledgebaseSourceDescriptor descriptor) {
        String newName = SwingDialogs.showInput(
                knowledgebaseSourcePanel,
                "Введите новое имя базы знаний:",
                "Переименование базы знаний",
                descriptor.name()
        );
        if (newName == null) {
            return;
        }

        try {
            ProjectStructure projectStructure = activeProject.requireProject();
            Path targetSourcePath = KnowledgebaseSourceUtility.resolveSourcePath(
                    projectStructure.knowledgebasePath(),
                    newName
            );
            if (descriptor.integrationPath().equals(targetSourcePath)) {
                return;
            }

            boolean reopenEditor = editorPanelPresenter.isOpen(descriptor.rulesPath().toFile());
            if (reopenEditor) {
                editorPanelPresenter.saveOpenFile(descriptor.rulesPath().toFile());
            }

            writeService.rename(projectStructure.knowledgebasePath(), descriptor, newName);
            knowledgebaseSourcePanel.showSources(readService.list(projectStructure.knowledgebasePath()));

            if (reopenEditor) {
                editorPanelPresenter.discardFile(descriptor.rulesPath().toFile());
                editorPanelPresenter.openFile(
                        KnowledgebaseSourceUtility.resolveRulesPath(targetSourcePath).toFile(),
                        KnowledgebaseSourceUtility.resolveName(targetSourcePath)
                );
                workspacePanel.enableEditor();
            }
        } catch (RuntimeException ex) {
            SwingDialogs.showError(knowledgebaseSourcePanel, ex.getMessage(), "Ошибка базы знаний");
        }
    }

    private void runMutation(Consumer<ProjectStructure> action) {
        try {
            ProjectStructure projectStructure = activeProject.requireProject();
            action.accept(projectStructure);
            knowledgebaseSourcePanel.showSources(readService.list(projectStructure.knowledgebasePath()));
        } catch (RuntimeException ex) {
            SwingDialogs.showError(knowledgebaseSourcePanel, ex.getMessage(), "Ошибка базы знаний");
        }
    }

}
