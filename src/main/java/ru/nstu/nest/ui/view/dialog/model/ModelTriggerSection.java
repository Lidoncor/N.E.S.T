package ru.nstu.nest.ui.view.dialog.model;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.FormSectionPanel;

import java.util.List;

final class ModelTriggerSection extends FormSectionPanel {

    private final javax.swing.JTextArea triggerFactsArea = createArea();

    ModelTriggerSection(ModelIntegrationDescriptor descriptor) {
        super("Триггер");
        addArea("Факты запуска", triggerFactsArea);

        if (descriptor != null) {
            triggerFactsArea.setText(String.join(System.lineSeparator(), descriptor.config().getTrigger().getFacts()));
        }
    }

    List<String> triggerFacts() {
        return DialogFieldValues.parseLines(triggerFactsArea.getText(), "Укажите хотя бы один факт запуска");
    }

}
