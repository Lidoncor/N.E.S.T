package ru.nstu.nest.ui.view.dialog.kb;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;
import ru.nstu.nest.ui.view.dialog.common.FormDialogShell;

import javax.swing.SwingUtilities;
import java.awt.Window;

@Component
public class KnowledgebaseSourceFormDialog {

    public KnowledgebaseSourceFormData showCreateDialog(java.awt.Component parent) {
        return showDialog(parent, "Создание базы знаний", new KnowledgebaseCreateFormPanel());
    }

    public KnowledgebaseSourceFormData showImportDialog(java.awt.Component parent) {
        return showDialog(parent, "Импорт базы знаний", new KnowledgebaseImportFormPanel());
    }

    public KnowledgebaseSourceFormData showEditDialog(
            java.awt.Component parent,
            KnowledgebaseSourceDescriptor descriptor
    ) {
        return showDialog(parent, "Параметры базы знаний", new KnowledgebaseEditFormPanel(descriptor));
    }

    private KnowledgebaseSourceFormData showDialog(
            java.awt.Component parent,
            String title,
            DialogFormPanel<KnowledgebaseSourceFormData> formPanel
    ) {
        return new FormDialogShell<>(resolveOwner(parent), title, formPanel).showDialog();
    }

    private Window resolveOwner(java.awt.Component parent) {
        return parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
    }

}
