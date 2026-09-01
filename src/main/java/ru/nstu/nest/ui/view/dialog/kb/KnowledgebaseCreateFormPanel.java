package ru.nstu.nest.ui.view.dialog.kb;

import ru.nstu.nest.files.properties.kb.KnowledgebaseAgentType;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;
import ru.nstu.nest.ui.view.dialog.common.FormGridPanel;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class KnowledgebaseCreateFormPanel extends FormGridPanel implements DialogFormPanel<KnowledgebaseSourceFormData> {

    private final JTextField nameField = new JTextField(28);
    private final JComboBox<KnowledgebaseAgentType> agentTypeCombo =
            new JComboBox<>(KnowledgebaseAgentType.values());

    KnowledgebaseCreateFormPanel() {
        agentTypeCombo.setSelectedItem(KnowledgebaseAgentType.FORWARD_CHAIN);
        addField("Имя файла", nameField);
        addField("Тип агента", agentTypeCombo);
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public KnowledgebaseSourceFormData toFormData() {
        String sourceName = DialogFieldValues.requireValue(nameField.getText(), "Укажите имя базы знаний");
        if (sourceName.toLowerCase().endsWith(".klb")) {
            throw new IllegalArgumentException("Введите имя базы знаний без расширения .klb");
        }
        return new KnowledgebaseSourceFormData(
                sourceName,
                null,
                (KnowledgebaseAgentType) agentTypeCombo.getSelectedItem()
        );
    }

}
