package ru.nstu.nest.ui.view.dialog.project;

import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;
import ru.nstu.nest.ui.model.ProjectSettingsFormData;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;
import ru.nstu.nest.ui.view.dialog.common.FormGridPanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

final class ProjectSettingsFormPanel extends FormGridPanel implements DialogFormPanel<ProjectSettingsFormData> {

    private static final int MIN_CYCLE_LIMIT = 1;
    private static final int MAX_CYCLE_LIMIT = 1_000_000;

    private final JSpinner inferenceCycleLimitSpinner;

    ProjectSettingsFormPanel(ProjectSettingsProperties settings) {
        inferenceCycleLimitSpinner = new JSpinner(new SpinnerNumberModel(
                settings.getInferenceCycleLimit(),
                MIN_CYCLE_LIMIT,
                MAX_CYCLE_LIMIT,
                1
        ));

        JComponent editor = inferenceCycleLimitSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setColumns(8);
        }

        addField("Максимум циклов вывода", inferenceCycleLimitSpinner);
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public ProjectSettingsFormData toFormData() {
        Object value = inferenceCycleLimitSpinner.getValue();
        int inferenceCycleLimit = ((Number) value).intValue();
        if (inferenceCycleLimit < MIN_CYCLE_LIMIT) {
            throw new IllegalArgumentException("Максимум циклов вывода должен быть больше 0");
        }

        return new ProjectSettingsFormData(inferenceCycleLimit);
    }

}
