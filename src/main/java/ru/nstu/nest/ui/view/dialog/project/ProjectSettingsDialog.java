package ru.nstu.nest.ui.view.dialog.project;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;
import ru.nstu.nest.ui.model.ProjectSettingsFormData;
import ru.nstu.nest.ui.view.dialog.common.FormDialogShell;

import javax.swing.SwingUtilities;
import java.awt.Window;

@Component
public class ProjectSettingsDialog {

    public ProjectSettingsFormData showDialog(
            java.awt.Component parent,
            ProjectSettingsProperties settings
    ) {
        return new FormDialogShell<>(
                resolveOwner(parent),
                "Настройки проекта",
                new ProjectSettingsFormPanel(settings)
        ).showDialog();
    }

    private Window resolveOwner(java.awt.Component parent) {
        return parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
    }

}
