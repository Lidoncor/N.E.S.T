package ru.nstu.nest.inference.source.model;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.properties.validator.ModelSourcePropertiesValidator;

import java.nio.file.Path;

@Component
public class OnnxClassificationModelFactory {

    private final ModelSourcePropertiesValidator validator;

    public OnnxClassificationModelFactory(ModelSourcePropertiesValidator validator) {
        this.validator = validator;
    }

    public OnnxClassificationModel create(Path modelPath, ModelSourceProperties properties) {
        OrtSession.SessionOptions sessionOptions = null;
        OrtSession session = null;

        try {
            OrtEnvironment environment = OrtEnvironment.getEnvironment();
            sessionOptions = new OrtSession.SessionOptions();
            session = environment.createSession(modelPath.toString(), sessionOptions);
            validator.validateAgainstSession(session, properties);

            return createModel(environment, session, sessionOptions, properties);
        } catch (IllegalArgumentException e) {
            RuntimeException closeFailure = closeOnFailure(session, sessionOptions);
            if (closeFailure != null) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        } catch (OrtException e) {
            RuntimeException closeFailure = closeOnFailure(session, sessionOptions);
            if (closeFailure != null) {
                e.addSuppressed(closeFailure);
            }
            throw new IllegalStateException("Не удалось инициализировать модель " + modelPath.getFileName(), e);
        }
    }

    private OnnxClassificationModel createModel(
            OrtEnvironment environment,
            OrtSession session,
            OrtSession.SessionOptions sessionOptions,
            ModelSourceProperties properties
    ) throws OrtException {
        return new OnnxClassificationModel(
                environment,
                session,
                sessionOptions,
                properties.getInput().getTensorName(),
                resolveOutputTensorName(session, properties)
        );
    }

    private String resolveOutputTensorName(OrtSession session, ModelSourceProperties properties) {
        String configuredTensorName = properties.getOutput().getTensorName();
        if (configuredTensorName != null && !configuredTensorName.isBlank()) {
            return configuredTensorName;
        }
        return session.getOutputNames().iterator().next();
    }

    private RuntimeException closeOnFailure(OrtSession session, OrtSession.SessionOptions sessionOptions) {
        RuntimeException closeFailure = null;
        closeFailure = closeQuietly(session, "Не удалось закрыть сессию модели после ошибки инициализации", closeFailure);
        closeFailure = closeQuietly(
                sessionOptions,
                "Не удалось закрыть параметры сессии модели после ошибки инициализации",
                closeFailure
        );
        return closeFailure;
    }

    private RuntimeException closeQuietly(
            AutoCloseable resource,
            String errorMessage,
            RuntimeException currentFailure
    ) {
        if (resource == null) {
            return currentFailure;
        }

        try {
            resource.close();
            return currentFailure;
        } catch (Exception e) {
            RuntimeException failure = currentFailure != null
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
