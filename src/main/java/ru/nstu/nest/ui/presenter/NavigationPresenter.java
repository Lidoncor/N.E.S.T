package ru.nstu.nest.ui.presenter;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.service.project.ProjectStructure;
import ru.nstu.nest.ui.coordinator.ProjectRunCoordinator;
import ru.nstu.nest.files.service.project.ProjectService;
import ru.nstu.nest.ui.view.component.NavigationBar;
import ru.nstu.nest.ui.view.panel.ProjectRunStatusPanel;
import ru.nstu.nest.ui.view.panel.WorkspacePanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.File;

@Component
public class NavigationPresenter {

    private final NavigationBar navigationBar;
    private final EditorPanelPresenter editorPanelPresenter;
    private final ProjectService projectService;
    private final ProjectRunCoordinator projectRunCoordinator;
    private final KnowledgebaseSourcePresenter knowledgebaseSourcePresenter;
    private final ModelIntegrationPresenter modelIntegrationPresenter;
    private final WorkspacePanel workspacePanel;
    private final ProjectRunStatusPanel projectRunStatusPanel;

    public NavigationPresenter(
            NavigationBar navigationBar,
            EditorPanelPresenter editorPanelPresenter,
            ProjectService projectService,
            ProjectRunCoordinator projectRunCoordinator,
            KnowledgebaseSourcePresenter knowledgebaseSourcePresenter,
            ModelIntegrationPresenter modelIntegrationPresenter,
            WorkspacePanel workspacePanel,
            ProjectRunStatusPanel projectRunStatusPanel
    ) {
        this.navigationBar = navigationBar;
        this.editorPanelPresenter = editorPanelPresenter;
        this.projectService = projectService;
        this.projectRunCoordinator = projectRunCoordinator;
        this.knowledgebaseSourcePresenter = knowledgebaseSourcePresenter;
        this.modelIntegrationPresenter = modelIntegrationPresenter;
        this.workspacePanel = workspacePanel;
        this.projectRunStatusPanel = projectRunStatusPanel;

        bind();
    }

    private void bind() {
        navigationBar.onOpenProject(this::openProject);
        navigationBar.onCreateProject(this::createProject);
        navigationBar.onSave(editorPanelPresenter::saveSelectedFile);
        projectRunStatusPanel.onRun(this::runProject);
        projectRunStatusPanel.onStop(this::stopProject);
        projectRunStatusPanel.onSettings(this::showProjectSettings);
    }

    public void runProject(JFrame parent) {
        try {
            projectRunCoordinator.runProject(parent);
        } catch (RuntimeException ex) {
            navigationBar.showError(ex.getMessage());
        }
    }

    public void stopProject() {
        try {
            projectRunCoordinator.stopProject();
        } catch (RuntimeException ex) {
            navigationBar.showError(ex.getMessage());
        }
    }

    public void showProjectSettings(JFrame parent) {
        try {
            projectRunCoordinator.showSettings(parent);
        } catch (RuntimeException ex) {
            navigationBar.showError(ex.getMessage());
        }
    }

    public void openProject() {
        handleProjectSelection(navigationBar.chooseDirectoryToOpenProject(), false);
    }

    public void createProject() {
        handleProjectSelection(navigationBar.chooseDirectoryToCreateProject(), true);
    }

    private void handleProjectSelection(File directory, boolean createNew) {
        if (directory == null) {
            return;
        }

        try {
            ProjectStructure projectStructure = createNew
                    ? projectService.createProject(directory)
                    : projectService.openProject(directory);
            knowledgebaseSourcePresenter.showProject(projectStructure);
            modelIntegrationPresenter.showProject(projectStructure);
            workspacePanel.enableProjectSidebar();
            workspacePanel.enableEditor();
            projectRunStatusPanel.setProjectReady();
            navigationBar.setSaveEnabled(true);
            updateWindowTitle(projectStructure);
        } catch (RuntimeException ex) {
            navigationBar.showError(ex.getMessage());
        }
    }

    private void updateWindowTitle(ProjectStructure projectStructure) {
        java.awt.Window window = SwingUtilities.getWindowAncestor(navigationBar);
        if (window instanceof JFrame frame) {
            frame.setTitle("NEST - " + projectStructure.name());
        }
    }

}

