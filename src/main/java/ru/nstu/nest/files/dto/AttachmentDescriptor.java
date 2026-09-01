package ru.nstu.nest.files.dto;

import ru.nstu.nest.files.properties.attachment.AttachmentType;

public record AttachmentDescriptor(
        String id,
        String name,
        AttachmentType type,
        String path,
        String mimeType
) {
}
