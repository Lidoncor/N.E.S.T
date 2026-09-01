package ru.nstu.nest.ui.view.dialog.kb;

import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.files.properties.kb.KnowledgebaseAgentType;
import ru.nstu.nest.ui.model.KnowledgebaseSourceFormData;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;
import ru.nstu.nest.ui.view.dialog.common.FormGridPanel;

import javax.swing.JComboBox;
import javax.swing.JPanel;

final class KnowledgebaseEditFormPanel extends FormGridPanel implements DialogFormPanel<KnowledgebaseSourceFormData> {

    private final JComboBox<KnowledgebaseAgentType> agentTypeCombo =
            new JComboBox<>(KnowledgebaseAgentType.values());

    KnowledgebaseEditFormPanel(KnowledgebaseSourceDescriptor descriptor) {
        agentTypeCombo.setSelectedItem(descriptor.config().getAgentType());
        addField("Тип агента", agentTypeCombo);
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public KnowledgebaseSourceFormData toFormData() {
        return new KnowledgebaseSourceFormData(
                null,
                null,
                (KnowledgebaseAgentType) agentTypeCombo.getSelectedItem()
        );
    }

}
