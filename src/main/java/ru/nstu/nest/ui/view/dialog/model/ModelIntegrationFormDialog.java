package ru.nstu.nest.ui.view.dialog.model;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.ui.model.ModelIntegrationFormData;
import ru.nstu.nest.ui.view.dialog.common.FormDialogShell;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Window;

@Component
public class ModelIntegrationFormDialog {

    private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(860, 680);
    private static final Dimension MINIMUM_DIALOG_SIZE = new Dimension(760, 460);

    public ModelIntegrationFormData showImportDialog(java.awt.Component parent) {
        return showDialog(parent, "Импорт модели", null);
    }

    public ModelIntegrationFormData showEditDialog(
            java.awt.Component parent,
            ModelIntegrationDescriptor descriptor
    ) {
        return showDialog(parent, "Параметры модели", descriptor);
    }

    private ModelIntegrationFormData showDialog(
            java.awt.Component parent,
            String title,
            ModelIntegrationDescriptor descriptor
    ) {
        return new FormDialogShell<>(
                resolveOwner(parent),
                title,
                new ModelIntegrationFormPanel(descriptor),
                DEFAULT_DIALOG_SIZE,
                MINIMUM_DIALOG_SIZE,
                true
        ).showDialog();
    }

    private Window resolveOwner(java.awt.Component parent) {
        return parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
    }

}
