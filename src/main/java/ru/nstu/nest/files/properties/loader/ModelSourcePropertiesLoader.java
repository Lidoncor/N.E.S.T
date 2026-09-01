package ru.nstu.nest.files.properties.loader;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModelSourcePropertiesLoader {

    public ModelSourceProperties load(Path path) {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(ModelSourceProperties.class, options));

        try (InputStream inputStream = Files.newInputStream(path)) {
            ModelSourceProperties config = yaml.load(inputStream);
            if (config == null) {
                throw new IllegalStateException("Параметры модели пустые: " + path.getFileName());
            }
            return config;
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать параметры модели " + path.getFileName(), e);
        }
    }

    public void save(Path path, ModelSourceProperties config) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        try {
            Files.writeString(path, yaml.dump(toMap(config)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать параметры модели " + path.getFileName(), e);
        }
    }

    private Map<String, Object> toMap(ModelSourceProperties config) {
        Map<String, Object> root = new LinkedHashMap<>();

        root.put("model", Map.of("taskType", config.getModel().getTaskType().name()));
        root.put("trigger", Map.of("facts", config.getTrigger().getFacts()));
        root.put("input", toInputMap(config));

        Map<String, Object> output = new LinkedHashMap<>();
        if (hasText(config.getOutput().getTensorName())) {
            output.put("tensorName", config.getOutput().getTensorName());
        }
        output.put("classMapping", toClassMapping(config));
        output.put("resultMapping", Map.of("fact", config.getOutput().getResultMapping().getFact()));
        root.put("output", output);

        return root;
    }

    private Map<String, Object> toInputMap(ModelSourceProperties config) {
        Map<String, Object> input = new LinkedHashMap<>();
        ModelInputType inputType = resolveInputType(config.getInput());
        input.put("type", inputType.name());
        input.put("tensorName", config.getInput().getTensorName());
        if (inputType == ModelInputType.IMAGE_FACT) {
            input.put("image", toImageMap(config.getInput().getImage()));
            return input;
        }
        input.put("features", toInputFeatures(config));
        return input;
    }

    private Map<String, Object> toImageMap(ModelSourceProperties.Image image) {
        Map<String, Object> imageMap = new LinkedHashMap<>();
        imageMap.put("fact", image.getFact());
        imageMap.put("width", image.getWidth());
        imageMap.put("height", image.getHeight());
        imageMap.put("colorMode", image.getColorMode().name());
        imageMap.put("layout", image.getLayout().name());
        imageMap.put("normalize", image.isNormalize());
        return imageMap;
    }

    private List<Map<String, Object>> toInputFeatures(ModelSourceProperties config) {
        return config.getInput().getFeatures().stream()
                .sorted(Comparator.comparingInt(ModelSourceProperties.Feature::getIndex))
                .map(feature -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("index", feature.getIndex());
                    item.put("fact", feature.getFact());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> toClassMapping(ModelSourceProperties config) {
        return config.getOutput().getClassMapping().stream()
                .sorted(Comparator.comparingInt(ModelSourceProperties.ClassMapping::getIndex))
                .map(mapping -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("index", mapping.getIndex());
                    item.put("label", mapping.getLabel());
                    item.put("fact", mapping.getFact());
                    return item;
                })
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ModelInputType resolveInputType(ModelSourceProperties.Input input) {
        if (input == null || input.getType() == null) {
            return ModelInputType.FACT_VECTOR;
        }
        return input.getType();
    }

}
