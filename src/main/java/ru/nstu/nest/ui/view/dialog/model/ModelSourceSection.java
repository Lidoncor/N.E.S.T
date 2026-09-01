package ru.nstu.nest.ui.view.dialog.model;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.TaskType;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.FormSectionPanel;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

final class ModelSourceSection extends FormSectionPanel {

    private final JTextField modelPathField = new JTextField(28);
    private final JComboBox<TaskType> taskTypeCombo = new JComboBox<>(new TaskType[]{TaskType.CLASSIFICATION});
    private final boolean editing;
    private Consumer<Path> modelPathChanged = _ -> {};

    ModelSourceSection(ModelIntegrationDescriptor descriptor) {
        super("Источник");
        editing = descriptor != null;

        if (!editing) {
            addBrowseField("Файл модели", modelPathField, "Выбрать", this::browseModelFile);
            modelPathField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    notifyModelPathChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    notifyModelPathChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    notifyModelPathChanged();
                }
            });
        }
        addField("Тип задачи", taskTypeCombo);

        taskTypeCombo.setSelectedItem(TaskType.CLASSIFICATION);
        if (editing) {
            taskTypeCombo.setSelectedItem(descriptor.config().getModel().getTaskType());
        }
    }

    Path sourceModelPath() {
        return editing
                ? null
                : DialogFieldValues.requireExistingFile(modelPathField.getText(), ".onnx", "Файл модели");
    }

    TaskType taskType() {
        return (TaskType) taskTypeCombo.getSelectedItem();
    }

    void onModelPathChanged(Consumer<Path> listener) {
        modelPathChanged = listener != null ? listener : _ -> {};
        notifyModelPathChanged();
    }

    private void browseModelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл модели");
        chooser.setApproveButtonText("Выбрать");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (SwingDialogs.showOpenDialog(chooser, this) == JFileChooser.APPROVE_OPTION) {
            modelPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void notifyModelPathChanged() {
        if (editing) {
            return;
        }

        String pathText = DialogFieldValues.optionalValue(modelPathField.getText());
        if (pathText == null) {
            modelPathChanged.accept(null);
            return;
        }

        Path path = Path.of(pathText);
        if (!Files.isRegularFile(path) || !pathText.toLowerCase().endsWith(".onnx")) {
            modelPathChanged.accept(null);
            return;
        }

        modelPathChanged.accept(path);
    }

}
