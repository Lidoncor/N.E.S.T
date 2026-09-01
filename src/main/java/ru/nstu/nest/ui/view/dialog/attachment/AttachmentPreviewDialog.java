package ru.nstu.nest.ui.view.dialog.attachment;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public final class AttachmentPreviewDialog {

    private static final Dimension DEFAULT_SIZE = new Dimension(720, 520);

    private AttachmentPreviewDialog() {
    }

    public static void show(java.awt.Component parent, Path imagePath, String title) {
        BufferedImage image = readImage(imagePath);
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = new JDialog(owner, title == null || title.isBlank() ? "Attachment" : title);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(new JLabel(new ImageIcon(image))), BorderLayout.CENTER);
        dialog.setSize(DEFAULT_SIZE);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static BufferedImage readImage(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                throw new IllegalArgumentException("Файл attachment не является изображением: " + imagePath.getFileName());
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось прочитать attachment: " + imagePath.getFileName(), e);
        }
    }
}
