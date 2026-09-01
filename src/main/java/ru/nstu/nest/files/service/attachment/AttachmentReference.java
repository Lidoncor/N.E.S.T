package ru.nstu.nest.files.service.attachment;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class AttachmentReference {

    public static final String PREFIX = "attachment:";

    public String format(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Attachment id не должен быть пустым");
        }
        return PREFIX + id.trim();
    }

    public boolean isReference(String value) {
        return value != null && value.trim().startsWith(PREFIX) && parseId(value).isPresent();
    }

    public Optional<String> parseId(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        if (!trimmed.startsWith(PREFIX)) {
            return Optional.empty();
        }

        String id = trimmed.substring(PREFIX.length()).trim();
        if (id.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    public String requireId(String value) {
        return parseId(value)
                .orElseThrow(() -> new IllegalArgumentException("Значение факта не является attachment-ссылкой: " + value));
    }
}
