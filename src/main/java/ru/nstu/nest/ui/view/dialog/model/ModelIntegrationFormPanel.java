package ru.nstu.nest.ui.view.dialog.model;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.ImageInputTensorSpec;
import ru.nstu.nest.ui.model.ModelIntegrationFormData;
import ru.nstu.nest.ui.view.dialog.common.DialogFormPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

final class ModelIntegrationFormPanel extends JPanel implements DialogFormPanel<ModelIntegrationFormData> {

    private final ModelSourceSection sourceSection;
    private final ModelTriggerSection triggerSection;
    private final ModelInputSection inputSection;
    private final ModelOutputSection outputSection;

    ModelIntegrationFormPanel(ModelIntegrationDescriptor descriptor) {
        sourceSection = new ModelSourceSection(descriptor);
        triggerSection = new ModelTriggerSection(descriptor);
        inputSection = new ModelInputSection(descriptor);
        outputSection = new ModelOutputSection(descriptor);
        sourceSection.onModelPathChanged(this::updateModelHints);
        if (descriptor != null) {
            updateModelHints(descriptor.modelPath());
        }
        initUi();
    }

    @Override
    public JPanel panel() {
        return this;
    }

    @Override
    public ModelIntegrationFormData toFormData() {
        return new ModelIntegrationFormData(
                sourceSection.sourceModelPath(),
                sourceSection.taskType(),
                triggerSection.triggerFacts(),
                inputSection.inputTensorName(),
                inputSection.inputType(),
                outputSection.outputTensorName(),
                inputSection.features(),
                inputSection.imageInput(),
                outputSection.classMapping(),
                outputSection.resultFact()
        );
    }

    private void initUi() {
        setLayout(new BorderLayout());
        add(buildScrollContent(), BorderLayout.CENTER);
    }

    private JScrollPane buildScrollContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        addSection(content, sourceSection);
        addSection(content, triggerSection);
        addSection(content, inputSection);
        content.add(outputSection);
        outputSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setBlockIncrement(120);
        return scrollPane;
    }

    private void addSection(JPanel content, JPanel section) {
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(section);
        content.add(Box.createVerticalStrut(8));
    }

    private void updateModelHints(Path modelPath) {
        if (modelPath == null) {
            inputSection.clearModelHint();
            outputSection.clearModelHint();
            return;
        }

        try (
                OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
                OrtSession session = OrtEnvironment.getEnvironment().createSession(modelPath.toString(), sessionOptions)
        ) {
            applyInputHint(session);
            applyOutputHint(session);
        } catch (OrtException | RuntimeException ex) {
            inputSection.clearModelHint();
            outputSection.clearModelHint();
        }
    }

    private void applyInputHint(OrtSession session) throws OrtException {
        TensorHint hint = resolveHint(
                session.getInputInfo(),
                inputSection.currentTensorName(),
                "Доступные входы"
        );
        if (hint == null) {
            inputSection.clearModelHint();
            return;
        }
        inputSection.applyModelHint(hint.tensorName(), hint.description(), hint.expectedSize(), hint.imageSpec());
    }

    private void applyOutputHint(OrtSession session) throws OrtException {
        TensorHint hint = resolveHint(
                session.getOutputInfo(),
                outputSection.currentTensorName(),
                "Доступные выходы"
        );
        if (hint == null) {
            outputSection.clearModelHint();
            return;
        }
        outputSection.applyModelHint(hint.tensorName(), hint.description(), hint.expectedSize());
    }

    private TensorHint resolveHint(
            Map<String, NodeInfo> info,
            String preferredTensorName,
            String fallbackLabel
    ) {
        if (info == null || info.isEmpty()) {
            return null;
        }

        if (preferredTensorName != null && info.containsKey(preferredTensorName)) {
            return toTensorHint(preferredTensorName, info.get(preferredTensorName));
        }
        if (info.size() == 1) {
            Map.Entry<String, NodeInfo> entry = info.entrySet().iterator().next();
            return toTensorHint(entry.getKey(), entry.getValue());
        }

        return new TensorHint(
                null,
                fallbackLabel + ": " + String.join(", ", info.keySet()),
                null,
                null
        );
    }

    private TensorHint toTensorHint(String tensorName, NodeInfo nodeInfo) {
        if (!(nodeInfo.getInfo() instanceof TensorInfo tensorInfo)) {
            return new TensorHint(tensorName, tensorName + " (не тензорный выход)", null, null);
        }

        String description = tensorName
                + " - "
                + tensorInfo.type
                + " "
                + formatShape(tensorInfo.getShape());
        Integer expectedSize = extractExpectedSize(tensorInfo);
        if (expectedSize != null) {
            description += ", ожидаемых значений: " + expectedSize;
        }
        Optional<ImageInputTensorSpec> imageSpec = ImageInputTensorSpec.infer(tensorInfo.getShape());
        if (tensorInfo.getShape().length == 4 && imageSpec.isEmpty()) {
            description += ". Не удалось определить параметры изображения из ONNX-модели. "
                    + "Укажите ширину, высоту и layout вручную.";
        }

        return new TensorHint(tensorName, description, expectedSize, imageSpec.orElse(null));
    }

    private String formatShape(long[] shape) {
        String[] parts = Arrays.stream(shape)
                .mapToObj(value -> value > 0 ? Long.toString(value) : "?")
                .toArray(String[]::new);
        return "[" + String.join(", ", parts) + "]";
    }

    private Integer extractExpectedSize(TensorInfo tensorInfo) {
        long[] shape = tensorInfo.getShape();
        if (shape.length == 1 && shape[0] > 0) {
            return (int) shape[0];
        }
        if (shape.length == 2 && shape[1] > 0) {
            return (int) shape[1];
        }
        return null;
    }

    private record TensorHint(
            String tensorName,
            String description,
            Integer expectedSize,
            ImageInputTensorSpec imageSpec
    ) {
    }

}
