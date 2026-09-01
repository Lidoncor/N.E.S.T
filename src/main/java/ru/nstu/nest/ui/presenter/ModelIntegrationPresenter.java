package ru.nstu.nest.ui.presenter;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.service.model.ModelIntegrationReadService;
import ru.nstu.nest.files.service.model.ModelIntegrationWriteService;
import ru.nstu.nest.files.service.project.ActiveProject;
import ru.nstu.nest.files.service.project.ProjectStructure;
import ru.nstu.nest.files.service.utility.ModelIntegrationUtility;
import ru.nstu.nest.ui.model.ModelIntegrationFormData;
import ru.nstu.nest.ui.presenter.actions.ModelIntegrationPanelActions;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.model.ModelIntegrationFormDialog;
import ru.nstu.nest.ui.view.panel.ModelIntegrationPanel;

@Component
public class ModelIntegrationPresenter {

    private final ActiveProject activeProject;
    private final ModelIntegrationReadService readService;
    private final ModelIntegrationWriteService writeService;
    private final ModelIntegrationPanel modelIntegrationPanel;
    private final ModelIntegrationFormDialog modelIntegrationFormDialog;

    public ModelIntegrationPresenter(
            ActiveProject activeProject,
            ModelIntegrationReadService readService,
            ModelIntegrationWriteService writeService,
            ModelIntegrationPanel modelIntegrationPanel,
            ModelIntegrationFormDialog modelIntegrationFormDialog
    ) {
        this.activeProject = activeProject;
        this.readService = readService;
        this.writeService = writeService;
        this.modelIntegrationPanel = modelIntegrationPanel;
        this.modelIntegrationFormDialog = modelIntegrationFormDialog;

        bind();
    }

    private void bind() {
        modelIntegrationPanel.bind(new ModelIntegrationPanelActions() {
            @Override
            public void importExisting() {
                handleImportModel();
            }

            @Override
            public void edit(ModelIntegrationDescriptor descriptor) {
                handleEditModel(descriptor);
            }

            @Override
            public void rename(ModelIntegrationDescriptor descriptor) {
                handleRenameModel(descriptor);
            }

            @Override
            public void delete(ModelIntegrationDescriptor descriptor) {
                handleDeleteModel(descriptor);
            }
        });
    }

    public void showProject(ProjectStructure projectStructure) {
        modelIntegrationPanel.showModels(readService.list(projectStructure.modelPath()));
    }

    private void handleImportModel() {
        ModelIntegrationFormData formData = modelIntegrationFormDialog.showImportDialog(modelIntegrationPanel);
        if (formData == null) {
            return;
        }

        runMutation(projectStructure -> writeService.save(projectStructure.modelPath(), formData, null));
    }

    private void handleEditModel(ModelIntegrationDescriptor descriptor) {
        ModelIntegrationFormData formData = modelIntegrationFormDialog.showEditDialog(modelIntegrationPanel, descriptor);
        if (formData == null) {
            return;
        }

        runMutation(projectStructure -> writeService.save(projectStructure.modelPath(), formData, descriptor));
    }

    private void handleDeleteModel(ModelIntegrationDescriptor descriptor) {
        int choice = SwingDialogs.showConfirm(
                modelIntegrationPanel,
                "Удалить модель " + descriptor.name() + "?",
                "Удаление модели",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        runMutation(projectStructure -> writeService.delete(descriptor));
    }

    private void handleRenameModel(ModelIntegrationDescriptor descriptor) {
        String newName = SwingDialogs.showInput(
                modelIntegrationPanel,
                "Введите новое имя модели:",
                "Переименование модели",
                descriptor.name()
        );
        if (newName == null) {
            return;
        }

        runMutation(projectStructure -> {
            java.nio.file.Path targetIntegrationPath =
                    projectStructure.modelPath().resolve(ModelIntegrationUtility.normalizeName(newName));
            if (!descriptor.integrationPath().equals(targetIntegrationPath)) {
                writeService.rename(projectStructure.modelPath(), descriptor, newName);
            }
        });
    }

    private void runMutation(java.util.function.Consumer<ProjectStructure> action) {
        try {
            ProjectStructure projectStructure = activeProject.requireProject();
            action.accept(projectStructure);
            modelIntegrationPanel.showModels(readService.list(projectStructure.modelPath()));
        } catch (RuntimeException ex) {
            SwingDialogs.showError(modelIntegrationPanel, ex.getMessage(), "Ошибка модели");
        }
    }

}
