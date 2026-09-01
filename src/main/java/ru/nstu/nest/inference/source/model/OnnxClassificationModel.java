package ru.nstu.nest.inference.source.model;

import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ru.nstu.nest.inference.dto.InputVector;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;

public class OnnxClassificationModel implements AutoCloseable {

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final OrtSession.SessionOptions sessionOptions;
    private final String inputTensorName;
    private final String outputTensorName;

    public OnnxClassificationModel(
            OrtEnvironment environment,
            OrtSession session,
            OrtSession.SessionOptions sessionOptions,
            String inputTensorName,
            String outputTensorName
    ) {
        this.environment = environment;
        this.session = session;
        this.sessionOptions = sessionOptions;
        this.inputTensorName = inputTensorName;
        this.outputTensorName = outputTensorName;
    }

    public float[] run(InputVector inputVector) {
        try (
                OnnxTensor inputTensor = createInputTensor(inputVector);
                OrtSession.Result result = session.run(Map.of(inputTensorName, inputTensor), Set.of(outputTensorName))
        ) {
            return readScores(result);
        } catch (OrtException e) {
            throw new IllegalStateException("Не удалось выполнить модель", e);
        }
    }

    public float[] run(float[] inputVector) {
        return run(new InputVector(java.util.List.of(), inputVector));
    }

    @Override
    public void close() {
        IllegalStateException closeFailure = null;
        closeFailure = closeResource(session, "Не удалось закрыть сессию модели", closeFailure);
        closeFailure = closeResource(sessionOptions, "Не удалось закрыть параметры сессии модели", closeFailure);

        if (closeFailure != null) {
            throw closeFailure;
        }
    }

    private OnnxTensor createInputTensor(InputVector inputVector) throws OrtException {
        return OnnxTensor.createTensor(
                environment,
                FloatBuffer.wrap(inputVector.values()),
                inputVector.shape()
        );
    }

    private float[] readScores(OrtSession.Result result) throws OrtException {
        OnnxValue outputValue = result.get(outputTensorName)
                .orElseThrow(() -> new IllegalStateException("Выходной тензор не был возвращен: " + outputTensorName));
        try (outputValue) {
            return extractScores(outputValue.getValue());
        }
    }

    private float[] extractScores(Object value) {
        if (value instanceof float[] vector) {
            return vector;
        }
        if (value instanceof float[][] matrix && matrix.length > 0) {
            return matrix[0];
        }
        throw new IllegalStateException("Неподдерживаемая форма выхода модели для классификации");
    }

    private IllegalStateException closeResource(
            AutoCloseable resource,
            String errorMessage,
            IllegalStateException currentFailure
    ) {
        try {
            resource.close();
            return currentFailure;
        } catch (Exception e) {
            IllegalStateException failure = currentFailure != null
                    ? currentFailure
                    : new IllegalStateException(errorMessage, e);
            if (failure != currentFailure) {
                return failure;
            }
            failure.addSuppressed(e);
            return failure;
        }
    }

}
