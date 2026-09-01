package ru.nstu.nest.files.service.attachment;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import ru.nstu.nest.files.dto.AttachmentDescriptor;
import ru.nstu.nest.files.properties.attachment.AttachmentType;
import ru.nstu.nest.files.service.project.ProjectStructure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AttachmentService {

    private static final String REGISTRY_FILE_NAME = "attachments.yml";
    private static final String ATTACHMENTS_DIR_NAME = "attachments";

    public AttachmentDescriptor createImageAttachment(ProjectStructure projectStructure, Path sourcePath) {
        if (projectStructure == null) {
            throw new IllegalArgumentException("Проект не открыт");
        }
        return createImageAttachment(projectStructure.rootPath(), sourcePath);
    }

    public AttachmentDescriptor createImageAttachment(Path projectRoot, Path sourcePath) {
        requireReadableFile(sourcePath);
        ensureImage(sourcePath);

        Path runtimePath = runtimePath(projectRoot);
        Path attachmentsPath = attachmentsPath(projectRoot);
        createDirectories(runtimePath, "Не удалось создать каталог runtime");
        createDirectories(attachmentsPath, "Не удалось создать каталог attachments");

        List<AttachmentDescriptor> descriptors = new ArrayList<>(list(projectRoot));
        String id = nextId(sourcePath, descriptors);
        String extension = extension(sourcePath);
        String storedFileName = id + extension;
        String relativePath = ATTACHMENTS_DIR_NAME + "/" + storedFileName;
        Path targetPath = runtimePath.resolve(relativePath);

        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить attachment " + sourcePath.getFileName(), e);
        }

        AttachmentDescriptor descriptor = new AttachmentDescriptor(
                id,
                sourcePath.getFileName().toString(),
                AttachmentType.IMAGE,
                relativePath,
                detectMimeType(sourcePath)
        );
        descriptors.add(descriptor);
        save(projectRoot, descriptors);
        return descriptor;
    }

    public List<AttachmentDescriptor> list(ProjectStructure projectStructure) {
        if (projectStructure == null) {
            return List.of();
        }
        return list(projectStructure.rootPath());
    }

    public List<AttachmentDescriptor> list(Path projectRoot) {
        Path registryPath = registryPath(projectRoot);
        if (!Files.isRegularFile(registryPath)) {
            return List.of();
        }

        try (InputStream inputStream = Files.newInputStream(registryPath)) {
            Object loaded = new Yaml().load(inputStream);
            if (!(loaded instanceof Map<?, ?> root)) {
                return List.of();
            }
            Object attachments = root.get("attachments");
            if (!(attachments instanceof List<?> entries)) {
                return List.of();
            }

            List<AttachmentDescriptor> descriptors = new ArrayList<>(entries.size());
            for (Object entry : entries) {
                if (entry instanceof Map<?, ?> map) {
                    descriptors.add(toDescriptor(map));
                }
            }
            return List.copyOf(descriptors);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать registry attachments", e);
        }
    }

    public Optional<AttachmentDescriptor> find(ProjectStructure projectStructure, String id) {
        if (projectStructure == null) {
            return Optional.empty();
        }
        return find(projectStructure.rootPath(), id);
    }

    public Optional<AttachmentDescriptor> find(Path projectRoot, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return list(projectRoot).stream()
                .filter(descriptor -> descriptor.id().equals(id))
                .findFirst();
    }

    public Path requireAttachmentPath(Path projectRoot, String id) {
        AttachmentDescriptor descriptor = find(projectRoot, id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment не найден: " + id));

        Path path = resolvePath(projectRoot, descriptor);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Файл attachment отсутствует: " + descriptor.path());
        }
        return path;
    }

    public Path resolvePath(ProjectStructure projectStructure, AttachmentDescriptor descriptor) {
        return resolvePath(projectStructure.rootPath(), descriptor);
    }

    public Path resolvePath(Path projectRoot, AttachmentDescriptor descriptor) {
        return runtimePath(projectRoot).resolve(descriptor.path()).normalize();
    }

    private void save(Path projectRoot, List<AttachmentDescriptor> descriptors) {
        createDirectories(runtimePath(projectRoot), "Не удалось создать каталог runtime");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("attachments", descriptors.stream().map(this::toMap).toList());

        try {
            Files.writeString(registryPath(projectRoot), new Yaml().dump(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить registry attachments", e);
        }
    }

    private AttachmentDescriptor toDescriptor(Map<?, ?> map) {
        return new AttachmentDescriptor(
                stringValue(map.get("id")),
                stringValue(map.get("name")),
                AttachmentType.valueOf(stringValue(map.get("type"))),
                stringValue(map.get("path")),
                stringValue(map.get("mimeType"))
        );
    }

    private Map<String, Object> toMap(AttachmentDescriptor descriptor) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", descriptor.id());
        item.put("name", descriptor.name());
        item.put("type", descriptor.type().name());
        item.put("path", descriptor.path());
        item.put("mimeType", descriptor.mimeType());
        return item;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private void requireReadableFile(Path sourcePath) {
        if (sourcePath == null || !Files.isRegularFile(sourcePath)) {
            throw new IllegalArgumentException("Файл attachment отсутствует: " + sourcePath);
        }
    }

    private void ensureImage(Path sourcePath) {
        try {
            BufferedImage image = ImageIO.read(sourcePath.toFile());
            if (image == null) {
                throw new IllegalArgumentException("Файл не является изображением: " + sourcePath.getFileName());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось прочитать изображение: " + sourcePath.getFileName(), e);
        }
    }

    private void createDirectories(Path path, String errorMessage) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private String nextId(Path sourcePath, List<AttachmentDescriptor> descriptors) {
        String base = sanitizeId(stem(sourcePath.getFileName().toString()));
        if (base.isBlank()) {
            base = "attachment";
        }

        String candidate = base;
        int suffix = 2;
        while (containsId(descriptors, candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean containsId(List<AttachmentDescriptor> descriptors, String id) {
        return descriptors.stream().anyMatch(descriptor -> descriptor.id().equals(id));
    }

    private String sanitizeId(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String stem(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
    }

    private String extension(Path sourcePath) {
        String fileName = sourcePath.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return "";
        }
        return fileName.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private String detectMimeType(Path sourcePath) {
        try {
            String mimeType = Files.probeContentType(sourcePath);
            if (mimeType != null && !mimeType.isBlank()) {
                return mimeType;
            }
        } catch (IOException ignored) {
            // Fall through to extension-based defaults.
        }

        String extension = extension(sourcePath);
        return switch (extension) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".bmp" -> "image/bmp";
            case ".webp" -> "image/webp";
            default -> "image/png";
        };
    }

    private Path runtimePath(Path projectRoot) {
        return projectRoot.resolve("runtime");
    }

    private Path attachmentsPath(Path projectRoot) {
        return runtimePath(projectRoot).resolve(ATTACHMENTS_DIR_NAME);
    }

    private Path registryPath(Path projectRoot) {
        return runtimePath(projectRoot).resolve(REGISTRY_FILE_NAME);
    }
}
