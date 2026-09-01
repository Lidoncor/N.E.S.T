package ru.nstu.nest.ui.presenter;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.service.project.ProjectService;
import ru.nstu.nest.ui.presenter.actions.EditorPanelActions;
import ru.nstu.nest.ui.view.panel.EditorPanel;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class EditorPanelPresenter {

    private final EditorPanel editorPanel;
    private final ProjectService projectService;
    private final Set<File> openedFiles = new LinkedHashSet<>();

    private File selectedFile;

    public EditorPanelPresenter(EditorPanel editorPanel, ProjectService projectService) {
        this.editorPanel = editorPanel;
        this.projectService = projectService;

        bind();
    }

    private void bind() {
        editorPanel.bind(new EditorPanelActions() {
            @Override
            public void closeRequested(File file) {
                handleCloseRequested(file);
            }

            @Override
            public void selectionChanged(File file) {
                handleSelectionChanged(file);
            }

            @Override
            public void saveRequested() {
                handleSaveRequested();
            }
        });
    }

    public void openFile(File file, String title) {
        if (openedFiles.contains(file)) {
            editorPanel.selectTab(file);
            return;
        }

        try {
            String content = projectService.readFile(file);
            openedFiles.add(file);
            editorPanel.showTab(file, title, content);
            selectedFile = file;
        } catch (RuntimeException ex) {
            editorPanel.showError(ex.getMessage());
        }
    }

    private void handleCloseRequested(File file) {
        if (!file.exists()) {
            discardFile(file);
            return;
        }

        try {
            saveFile(file);
            discardFile(file);
        } catch (RuntimeException ex) {
            editorPanel.showError(ex.getMessage());
        }
    }

    private void handleSelectionChanged(File file) {
        selectedFile = file;
    }

    public void saveSelectedFile() {
        handleSaveRequested();
    }

    public boolean isOpen(File file) {
        return openedFiles.contains(file);
    }

    public void saveOpenFile(File file) {
        if (!openedFiles.contains(file)) {
            return;
        }
        saveFile(file);
    }

    public void discardFile(File file) {
        openedFiles.remove(file);
        editorPanel.removeTab(file);
        if (file.equals(selectedFile)) {
            selectedFile = null;
        }
    }

    private void handleSaveRequested() {
        if (selectedFile == null) {
            return;
        }

        try {
            saveFile(selectedFile);
        } catch (RuntimeException ex) {
            editorPanel.showError(ex.getMessage());
        }
    }

    private void saveFile(File file) {
        projectService.writeFile(file, editorPanel.getContent(file));
    }

}
