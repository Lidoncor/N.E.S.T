package ru.nstu.nest.ui.view.dialog.common;

import ru.nstu.nest.ui.view.component.RoundedCardPanel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public class ProgressDialog extends JDialog {

    private static final Color DIALOG_BACKGROUND = new Color(241, 245, 249);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(15, 23, 42);
    private static final Color TRACK_BACKGROUND = new Color(226, 232, 240);
    private static final Color TRACK_BORDER = new Color(203, 213, 225);
    private static final Color PROGRESS_COLOR = new Color(37, 99, 235);
    private static final int CARD_ARC = 16;

    private final JFrame parent;
    private final String text;
    private JLabel label;
    private RoundedProgressIndicator progressIndicator;

    public ProgressDialog(JFrame parent, String text) {
        super(parent, "Запуск проекта", true);
        this.parent = parent;
        this.text = text;
        initUi();
    }

    public void setText(String text) {
        label.setText(text);
    }

    private void initUi() {
        label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));

        progressIndicator = new RoundedProgressIndicator();

        RoundedCardPanel card = new RoundedCardPanel(CARD_ARC);
        card.setLayout(new BorderLayout(0, 12));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        card.add(label, BorderLayout.NORTH);
        card.add(progressIndicator, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(DIALOG_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(card, BorderLayout.CENTER);

        setContentPane(content);
        setMinimumSize(new Dimension(360, 128));
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            progressIndicator.start();
        } else {
            progressIndicator.stop();
        }
        super.setVisible(visible);
    }

    @Override
    public void dispose() {
        progressIndicator.stop();
        super.dispose();
    }

    private static final class RoundedProgressIndicator extends JComponent {

        private static final int HEIGHT = 12;
        private static final int ARC = 12;
        private static final int TIMER_DELAY = 16;
        private static final double STEP = 0.006;

        private final Timer timer;
        private double progress;

        private RoundedProgressIndicator() {
            setPreferredSize(new Dimension(300, HEIGHT));
            timer = new Timer(TIMER_DELAY, _ -> {
                progress += STEP;
                if (progress > 1.0) {
                    progress = 0.0;
                }
                repaint();
            });
        }

        private void start() {
            progress = 0.0;
            timer.start();
            repaint();
        }

        private void stop() {
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int inset = 1;
                int width = Math.max(0, getWidth() - inset * 2);
                int height = Math.min(HEIGHT, Math.max(0, getHeight() - inset * 2));
                int y = (getHeight() - height) / 2;
                int fillWidth = Math.min(width, Math.max(height, (int) Math.round(width * progress)));

                RoundRectangle2D track = new RoundRectangle2D.Double(inset, y, width, height, ARC, ARC);
                g2.setColor(TRACK_BACKGROUND);
                g2.fill(track);

                Shape previousClip = g2.getClip();
                g2.setClip(track);
                g2.setColor(PROGRESS_COLOR);
                g2.fillRect(inset, y, fillWidth, height);
                g2.setClip(previousClip);

                g2.setColor(TRACK_BORDER);
                g2.draw(track);
            } finally {
                g2.dispose();
            }
        }
    }

}
