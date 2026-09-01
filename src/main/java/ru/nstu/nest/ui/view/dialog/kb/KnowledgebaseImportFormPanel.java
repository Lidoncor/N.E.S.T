package ru.nstu.nest.ui.view.dialog.kb;

import ru.nstu.nest.files.properties.kb.KnowledgebaseAgentType;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;
import ru.nstu.nest.ui.view.dialog.common.FormGridPanel;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class KnowledgebaseImportFormPanel extends FormGridPanel implements DialogFormPanel<KnowledgebaseSourceFormData> {

    private final JComboBox<KnowledgebaseAgentType> agentTypeCombo =
            new JComboBox<>(KnowledgebaseAgentType.values());
    private final JTextField rulesPathField = new JTextField(28);

    KnowledgebaseImportFormPanel() {
        agentTypeCombo.setSelectedItem(KnowledgebaseAgentType.FORWARD_CHAIN);
        addField("Тип агента", agentTypeCombo);
        addBrowseField("Файл .klb", rulesPathField, "Выбрать", this::browseRulesFile);
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public KnowledgebaseSourceFormData toFormData() {
        return new KnowledgebaseSourceFormData(
                null,
                DialogFieldValues.requireExistingFile(rulesPathField.getText(), ".klb", "Файл базы знаний"),
                (KnowledgebaseAgentType) agentTypeCombo.getSelectedItem()
        );
    }

    private void browseRulesFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл базы знаний");
        chooser.setApproveButtonText("Выбрать");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (SwingDialogs.showOpenDialog(chooser, this) == JFileChooser.APPROVE_OPTION) {
            rulesPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

}
