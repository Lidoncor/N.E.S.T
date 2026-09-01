package ru.nstu.nest.files.properties.loader;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProjectSettingsPropertiesLoader {

    public ProjectSettingsProperties load(Path path) {
        if (!Files.isRegularFile(path)) {
            return new ProjectSettingsProperties();
        }

        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(ProjectSettingsProperties.class, options));

        try (InputStream inputStream = Files.newInputStream(path)) {
            ProjectSettingsProperties config = yaml.load(inputStream);
            config = config != null ? config : new ProjectSettingsProperties();
            validate(config);
            return config;
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать настройки проекта " + path.getFileName(), e);
        }
    }

    public void save(Path path, ProjectSettingsProperties config) {
        validate(config);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("inferenceCycleLimit", config.getInferenceCycleLimit());

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, new Yaml(options).dump(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать настройки проекта " + path.getFileName(), e);
        }
    }

    private void validate(ProjectSettingsProperties config) {
        if (config.getInferenceCycleLimit() < 1) {
            throw new IllegalStateException("Лимит циклов вывода должен быть больше 0");
        }
    }

}
