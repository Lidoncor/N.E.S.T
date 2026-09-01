package ru.nstu.nest.files.properties.loader;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import ru.nstu.nest.files.properties.kb.KnowledgebaseSourceProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KnowledgebaseSourcePropertiesLoader {

    public KnowledgebaseSourceProperties load(Path path) {
        if (!Files.isRegularFile(path)) {
            return new KnowledgebaseSourceProperties();
        }

        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(KnowledgebaseSourceProperties.class, options));

        try (InputStream inputStream = Files.newInputStream(path)) {
            KnowledgebaseSourceProperties config = yaml.load(inputStream);
            return config != null ? config : new KnowledgebaseSourceProperties();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать параметры базы знаний " + path.getFileName(), e);
        }
    }

    public void save(Path path, KnowledgebaseSourceProperties config) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("agentType", config.getAgentType().name());

        try {
            Files.writeString(path, new Yaml(options).dump(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать параметры базы знаний " + path.getFileName(), e);
        }
    }

}
