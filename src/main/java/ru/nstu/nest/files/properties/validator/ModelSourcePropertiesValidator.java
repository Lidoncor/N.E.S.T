package ru.nstu.nest.files.properties.validator;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxJavaType;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.model.ImageColorMode;
import ru.nstu.nest.files.properties.model.ImageInputTensorSpec;
import ru.nstu.nest.files.properties.model.ImageTensorLayout;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.properties.model.TaskType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ModelSourcePropertiesValidator {

    public void validate(ModelSourceProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Параметры модели не должны быть пустыми");
        }
        if (properties.getModel() == null || properties.getModel().getTaskType() == null) {
            throw new IllegalArgumentException("Укажите тип задачи модели");
        }
        if (properties.getModel().getTaskType() != TaskType.CLASSIFICATION) {
            throw new IllegalArgumentException("Поддерживаются только модели классификации");
        }
        if (properties.getInput() == null) {
            throw new IllegalArgumentException("Укажите секцию входа модели");
        }
        if (properties.getInput().getTensorName() == null || properties.getInput().getTensorName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя входного тензора");
        }
        if (resolveInputType(properties) == ModelInputType.IMAGE_FACT) {
            validateImage(properties.getInput().getImage());
        } else {
            validateFeatures(properties.getInput().getFeatures());
        }
        if (properties.getOutput() == null) {
            throw new IllegalArgumentException("Укажите секцию выхода модели");
        }
        validateClassMapping(properties.getOutput().getClassMapping());
        if (properties.getOutput().getResultMapping() == null
                || properties.getOutput().getResultMapping().getFact() == null
                || properties.getOutput().getResultMapping().getFact().isBlank()) {
            throw new IllegalArgumentException("Укажите факт результата модели");
        }
    }

    public void validateAgainstSession(OrtSession session, ModelSourceProperties properties) throws OrtException {
        Map<String, NodeInfo> inputInfo = getTensorInfoMap(session.getInputInfo(), "входных");
        Map<String, NodeInfo> outputInfo = getTensorInfoMap(session.getOutputInfo(), "выходных");

        TensorInfo modelInput = requireTensorInfo(inputInfo, properties.getInput().getTensorName(), "входной");
        validateInputTensor(modelInput, properties);

        String outputTensorName = resolveOutputTensorName(session, properties);
        TensorInfo modelOutput = requireTensorInfo(outputInfo, outputTensorName, "выходной");
        validateOutputTensor(modelOutput, properties);
    }

    private Map<String, NodeInfo> getTensorInfoMap(Map<String, NodeInfo> info, String kind) {
        if (info == null || info.isEmpty()) {
            throw new IllegalArgumentException("Модель не содержит " + kind + " тензоров");
        }
        return info;
    }

    private TensorInfo requireTensorInfo(Map<String, NodeInfo> info, String tensorName, String kind) {
        NodeInfo node = info.get(tensorName);
        if (node == null) {
            throw new IllegalArgumentException("Модель не содержит " + kind + " тензор: " + tensorName);
        }
        if (!(node.getInfo() instanceof TensorInfo tensorInfo)) {
            throw new IllegalArgumentException("Узел модели " + kind + " " + tensorName + " не является тензором");
        }
        return tensorInfo;
    }

    private void validateInputTensor(TensorInfo tensorInfo, ModelSourceProperties properties) {
        requireTensorType(tensorInfo, OnnxJavaType.FLOAT, "вход");
        if (resolveInputType(properties) == ModelInputType.IMAGE_FACT) {
            validateImageInputTensor(tensorInfo, properties);
            return;
        }

        requireRank(tensorInfo, 2, "вход");
        long[] shape = tensorInfo.getShape();
        requireDimension(shape[0], 1, "Размер батча входного тензора");
        requireDimension(shape[1], properties.getInput().getFeatures().size(), "Размер признаков входного тензора");
    }

    private void validateImageInputTensor(TensorInfo tensorInfo, ModelSourceProperties properties) {
        ModelSourceProperties.Image image = properties.getInput().getImage();
        requireImageRank(tensorInfo);
        long[] shape = tensorInfo.getShape();
        validateImageShape(shape, image);
    }

    private void validateImageShape(long[] shape, ModelSourceProperties.Image image) {
        requireDimension(shape[0], 1, "Размер батча входного тензора изображения");

        int expectedChannels = ImageInputTensorSpec.channelCount(image.getColorMode());
        if (image.getLayout() == ImageTensorLayout.NCHW) {
            requireImageChannels(shape[1], expectedChannels);
            requireImageDimension(shape[2], image.getHeight(), "Высота входного тензора изображения");
            requireImageDimension(shape[3], image.getWidth(), "Ширина входного тензора изображения");
            return;
        }

        requireImageDimension(shape[1], image.getHeight(), "Высота входного тензора изображения");
        requireImageDimension(shape[2], image.getWidth(), "Ширина входного тензора изображения");
        requireImageChannels(shape[3], expectedChannels);
    }

    private void validateOutputTensor(TensorInfo tensorInfo, ModelSourceProperties properties) {
        requireTensorType(tensorInfo, OnnxJavaType.FLOAT, "выход");

        long[] shape = tensorInfo.getShape();
        int classCount = properties.getOutput().getClassMapping().size();
        if (shape.length == 1) {
            requireDimension(shape[0], classCount, "Размер классов выходного тензора");
            return;
        }
        if (shape.length == 2) {
            requireDimension(shape[0], 1, "Размер батча выходного тензора");
            requireDimension(shape[1], classCount, "Размер классов выходного тензора");
            return;
        }

        throw new IllegalArgumentException("Выходной тензор классификации должен иметь ранг 1 или 2");
    }

    private void requireTensorType(TensorInfo tensorInfo, OnnxJavaType expectedType, String kind) {
        if (tensorInfo.type != expectedType) {
            throw new IllegalArgumentException(
                    "Тензор модели (" + kind + ") должен использовать значения " + expectedType
                            + ", получено " + tensorInfo.type
            );
        }
    }

    private void requireRank(TensorInfo tensorInfo, int expectedRank, String kind) {
        if (tensorInfo.getShape().length != expectedRank) {
            throw new IllegalArgumentException(
                    "Тензор модели (" + kind + ") должен иметь ранг " + expectedRank + ", получено "
                            + tensorInfo.getShape().length
            );
        }
    }

    private void requireDimension(long actualDimension, long expectedDimension, String dimensionName) {
        if (actualDimension > 0 && actualDimension != expectedDimension) {
            throw new IllegalArgumentException(
                    dimensionName + " должен быть " + expectedDimension + ", получено " + actualDimension
            );
        }
    }

    private String resolveOutputTensorName(OrtSession session, ModelSourceProperties properties) {
        String outputTensorName = properties.getOutput().getTensorName();
        if (outputTensorName == null || outputTensorName.isBlank()) {
            if (session.getOutputNames().size() == 1) {
                return session.getOutputNames().iterator().next();
            }
            throw new IllegalArgumentException("Укажите имя выходного тензора для модели с несколькими выходами");
        }
        return outputTensorName;
    }

    private void requireImageRank(TensorInfo tensorInfo) {
        if (tensorInfo.getShape().length != 4) {
            throw new IllegalArgumentException("IMAGE_FACT поддерживает только rank 4 input tensor");
        }
    }

    private void requireImageChannels(long actualDimension, long expectedDimension) {
        if (actualDimension > 0 && actualDimension != expectedDimension) {
            throw new IllegalArgumentException(
                    "Для RGB ожидается 3 канала, для GRAYSCALE - 1 канал. Получено " + actualDimension
            );
        }
    }

    private void requireImageDimension(long actualDimension, long expectedDimension, String dimensionName) {
        if (actualDimension > 0 && actualDimension != expectedDimension) {
            throw new IllegalArgumentException(
                    "Размер входного тензора модели не соответствует параметрам изображения. "
                            + dimensionName + " должен быть " + expectedDimension + ", получено " + actualDimension
            );
        }
    }

    private void validateFeatures(List<ModelSourceProperties.Feature> features) {
        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException("Вход модели должен содержать хотя бы один признак");
        }

        Set<Integer> indexes = new HashSet<>();
        for (ModelSourceProperties.Feature feature : features) {
            if (feature == null) {
                throw new IllegalArgumentException("Входные признаки не должны содержать пустые элементы");
            }
            if (feature.getIndex() < 0) {
                throw new IllegalArgumentException("Индекс входного признака не должен быть отрицательным");
            }
            if (feature.getFact() == null || feature.getFact().isBlank()) {
                throw new IllegalArgumentException("Укажите факт входного признака");
            }
            if (!indexes.add(feature.getIndex())) {
                throw new IllegalArgumentException("Индекс входного признака должен быть уникальным: " + feature.getIndex());
            }
        }

        requireContiguousIndexes(indexes, features.size(), "Индексы входных признаков");
    }

    private void validateImage(ModelSourceProperties.Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Укажите параметры входного изображения");
        }
        if (image.getFact() == null || image.getFact().isBlank()) {
            throw new IllegalArgumentException("Укажите факт с attachment-ссылкой на изображение");
        }
        if (image.getWidth() <= 0) {
            throw new IllegalArgumentException("Ширина входного изображения должна быть положительной");
        }
        if (image.getHeight() <= 0) {
            throw new IllegalArgumentException("Высота входного изображения должна быть положительной");
        }
        if (image.getColorMode() == null) {
            throw new IllegalArgumentException("Укажите цветовой режим входного изображения");
        }
        if (image.getLayout() == null) {
            throw new IllegalArgumentException("Укажите layout входного изображения");
        }
    }

    private void validateClassMapping(List<ModelSourceProperties.ClassMapping> classMapping) {
        if (classMapping == null || classMapping.isEmpty()) {
            throw new IllegalArgumentException("Выход модели должен содержать хотя бы один класс");
        }

        Set<Integer> indexes = new HashSet<>();
        for (ModelSourceProperties.ClassMapping mapping : classMapping) {
            if (mapping == null) {
                throw new IllegalArgumentException("Соответствия классов не должны содержать пустые элементы");
            }
            if (mapping.getIndex() < 0) {
                throw new IllegalArgumentException("Индекс класса не должен быть отрицательным");
            }
            if (mapping.getLabel() == null || mapping.getLabel().isBlank()) {
                throw new IllegalArgumentException("Укажите метку класса");
            }
            if (mapping.getFact() == null || mapping.getFact().isBlank()) {
                throw new IllegalArgumentException("Укажите факт класса");
            }
            if (!indexes.add(mapping.getIndex())) {
                throw new IllegalArgumentException("Индекс класса должен быть уникальным: " + mapping.getIndex());
            }
        }

        requireContiguousIndexes(indexes, classMapping.size(), "Индексы соответствий классов");
    }

    private void requireContiguousIndexes(Set<Integer> indexes, int itemCount, String mappingName) {
        for (int expectedIndex = 0; expectedIndex < itemCount; expectedIndex++) {
            if (!indexes.contains(expectedIndex)) {
                throw new IllegalArgumentException(mappingName + " должны покрывать диапазон 0.." + (itemCount - 1));
            }
        }
    }

    private ModelInputType resolveInputType(ModelSourceProperties properties) {
        if (properties.getInput().getType() == null) {
            return ModelInputType.FACT_VECTOR;
        }
        return properties.getInput().getType();
    }

}
