package ru.nstu.nest.files.properties.model;

import java.util.Optional;

public record ImageInputTensorSpec(
        int width,
        int height,
        ImageColorMode colorMode,
        ImageTensorLayout layout
) {

    public static Optional<ImageInputTensorSpec> infer(long[] shape) {
        if (shape == null || shape.length != 4 || !isStatic(shape[0]) || shape[0] != 1) {
            return Optional.empty();
        }

        Optional<ImageInputTensorSpec> nchw = inferNchw(shape);
        if (nchw.isPresent()) {
            return nchw;
        }
        return inferNhwc(shape);
    }

    private static Optional<ImageInputTensorSpec> inferNchw(long[] shape) {
        if (!isStatic(shape[1]) || !isStatic(shape[2]) || !isStatic(shape[3])) {
            return Optional.empty();
        }

        ImageColorMode colorMode = colorModeByChannels(shape[1]);
        if (colorMode == null) {
            return Optional.empty();
        }
        return Optional.of(new ImageInputTensorSpec(
                Math.toIntExact(shape[3]),
                Math.toIntExact(shape[2]),
                colorMode,
                ImageTensorLayout.NCHW
        ));
    }

    private static Optional<ImageInputTensorSpec> inferNhwc(long[] shape) {
        if (!isStatic(shape[1]) || !isStatic(shape[2]) || !isStatic(shape[3])) {
            return Optional.empty();
        }

        ImageColorMode colorMode = colorModeByChannels(shape[3]);
        if (colorMode == null) {
            return Optional.empty();
        }
        return Optional.of(new ImageInputTensorSpec(
                Math.toIntExact(shape[2]),
                Math.toIntExact(shape[1]),
                colorMode,
                ImageTensorLayout.NHWC
        ));
    }

    public static int channelCount(ImageColorMode colorMode) {
        return switch (colorMode) {
            case RGB -> 3;
            case GRAYSCALE -> 1;
        };
    }

    public static boolean isStatic(long dimension) {
        return dimension > 0;
    }

    private static ImageColorMode colorModeByChannels(long channels) {
        if (channels == 3) {
            return ImageColorMode.RGB;
        }
        if (channels == 1) {
            return ImageColorMode.GRAYSCALE;
        }
        return null;
    }
}
