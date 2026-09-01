package ru.nstu.nest.files.properties.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ModelSourceProperties {

    private Model model = new Model();
    private Trigger trigger = new Trigger();
    private Input input = new Input();
    private Output output = new Output();

    @Data
    @NoArgsConstructor
    public static class Model {
        private TaskType taskType;
    }

    @Data
    @NoArgsConstructor
    public static class Input {
        private ModelInputType type = ModelInputType.FACT_VECTOR;
        private String tensorName;
        private List<Feature> features = new ArrayList<>();
        private Image image = new Image();
    }

    @Data
    @NoArgsConstructor
    public static class Image {
        private String fact;
        private int width;
        private int height;
        private ImageColorMode colorMode = ImageColorMode.RGB;
        private ImageTensorLayout layout = ImageTensorLayout.NCHW;
        private boolean normalize = true;
    }

    @Data
    @NoArgsConstructor
    public static class Trigger {
        private List<String> facts = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class Output {
        private String tensorName;
        private List<ClassMapping> classMapping = new ArrayList<>();
        private ResultMapping resultMapping = new ResultMapping();
    }

    @Data
    @NoArgsConstructor
    public static class Feature {
        private int index;
        private String fact;
    }

    @Data
    @NoArgsConstructor
    public static class ClassMapping {
        private int index;
        private String label;
        private String fact;
    }

    @Data
    @NoArgsConstructor
    public static class ResultMapping {
        private String fact;
    }

}
