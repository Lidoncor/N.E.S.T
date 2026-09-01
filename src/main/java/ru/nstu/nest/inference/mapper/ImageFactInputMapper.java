package ru.nstu.nest.inference.mapper;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.properties.model.ImageColorMode;
import ru.nstu.nest.files.properties.model.ImageInputTensorSpec;
import ru.nstu.nest.files.properties.model.ImageTensorLayout;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.files.service.attachment.AttachmentReference;
import ru.nstu.nest.files.service.attachment.AttachmentService;
import ru.nstu.nest.inference.context.WorkingMemory;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.InputVector;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Component
public class ImageFactInputMapper implements ModelInputMapper {

    private final AttachmentService attachmentService;

    public ImageFactInputMapper(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @Override
    public ModelInputType inputType() {
        return ModelInputType.IMAGE_FACT;
    }

    @Override
    public boolean hasRequiredFacts(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig) {
        return workingMemory.has(requireImageConfig(inputConfig).getFact());
    }

    @Override
    public InputVector map(WorkingMemory workingMemory, ModelSourceProperties.Input inputConfig, Path projectRoot) {
        ModelSourceProperties.Image imageConfig = requireImageConfig(inputConfig);
        Fact imageFact = selectFact(workingMemory, imageConfig.getFact());
        String attachmentId = AttachmentReference.requireId(imageFact.value());
        Path imagePath = attachmentService.requireAttachmentPath(projectRoot, attachmentId);
        BufferedImage image = readImage(imagePath);
        BufferedImage resized = resize(image, imageConfig.getWidth(), imageConfig.getHeight());
        float[] values = toTensor(resized, imageConfig);
        return new InputVector(
                List.of(imageFact),
                values,
                shape(imageConfig)
        );
    }

    @Override
    public Set<String> fallbackTriggeringFacts(ModelSourceProperties.Input inputConfig) {
        return Set.of(requireImageConfig(inputConfig).getFact());
    }

    private ModelSourceProperties.Image requireImageConfig(ModelSourceProperties.Input inputConfig) {
        if (inputConfig == null || inputConfig.getImage() == null) {
            throw new IllegalArgumentException("Конфиг IMAGE_FACT неполный: отсутствует секция image");
        }
        ModelSourceProperties.Image image = inputConfig.getImage();
        if (image.getFact() == null || image.getFact().isBlank()) {
            throw new IllegalArgumentException("Конфиг IMAGE_FACT неполный: укажите факт изображения");
        }
        if (image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IllegalArgumentException("Конфиг IMAGE_FACT неполный: width и height должны быть положительными");
        }
        if (image.getColorMode() == null) {
            throw new IllegalArgumentException("Конфиг IMAGE_FACT неполный: укажите colorMode");
        }
        if (image.getLayout() == null) {
            throw new IllegalArgumentException("Конфиг IMAGE_FACT неполный: укажите layout");
        }
        return image;
    }

    private Fact selectFact(WorkingMemory workingMemory, String address) {
        List<Fact> candidates = workingMemory.find(address);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Отсутствует факт с изображением: " + address);
        }
        if (candidates.size() > 1) {
            throw new IllegalArgumentException("Факт с изображением неоднозначен: " + address);
        }
        return candidates.getFirst();
    }

    private BufferedImage readImage(Path path) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                throw new IllegalArgumentException("Файл attachment не является изображением: " + path.getFileName());
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось прочитать изображение attachment: " + path.getFileName(), e);
        }
    }

    private BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    private float[] toTensor(BufferedImage image, ModelSourceProperties.Image imageConfig) {
        if (imageConfig.getLayout() == ImageTensorLayout.NHWC) {
            return toNhwcTensor(image, imageConfig.getColorMode(), imageConfig.isNormalize());
        }
        return toNchwTensor(image, imageConfig.getColorMode(), imageConfig.isNormalize());
    }

    private float[] toNchwTensor(BufferedImage image, ImageColorMode colorMode, boolean normalize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int channelSize = width * height;
        int channelCount = ImageInputTensorSpec.channelCount(colorMode);
        float[] values = new float[channelCount * channelSize];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int pixelIndex = y * width + x;
                if (colorMode == ImageColorMode.GRAYSCALE) {
                    values[pixelIndex] = scale(gray(rgb), normalize);
                    continue;
                }
                values[pixelIndex] = scale((rgb >> 16) & 0xFF, normalize);
                values[channelSize + pixelIndex] = scale((rgb >> 8) & 0xFF, normalize);
                values[2 * channelSize + pixelIndex] = scale(rgb & 0xFF, normalize);
            }
        }

        return values;
    }

    private float[] toNhwcTensor(BufferedImage image, ImageColorMode colorMode, boolean normalize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int channelCount = ImageInputTensorSpec.channelCount(colorMode);
        float[] values = new float[height * width * channelCount];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int baseIndex = (y * width + x) * channelCount;
                if (colorMode == ImageColorMode.GRAYSCALE) {
                    values[baseIndex] = scale(gray(rgb), normalize);
                    continue;
                }
                values[baseIndex] = scale((rgb >> 16) & 0xFF, normalize);
                values[baseIndex + 1] = scale((rgb >> 8) & 0xFF, normalize);
                values[baseIndex + 2] = scale(rgb & 0xFF, normalize);
            }
        }

        return values;
    }

    private long[] shape(ModelSourceProperties.Image imageConfig) {
        int channels = ImageInputTensorSpec.channelCount(imageConfig.getColorMode());
        if (imageConfig.getLayout() == ImageTensorLayout.NHWC) {
            return new long[]{1, imageConfig.getHeight(), imageConfig.getWidth(), channels};
        }
        return new long[]{1, channels, imageConfig.getHeight(), imageConfig.getWidth()};
    }

    private float scale(int value, boolean normalize) {
        return normalize ? value / 255.0f : value;
    }

    private int gray(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
    }
}
