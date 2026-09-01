package ru.nstu.nest.ui.view.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class RoundedIconButton extends JButton {

    private static final Color DEFAULT_HOVER_BACKGROUND = new Color(223, 228, 233);
    private static final Color DEFAULT_PRESSED_BACKGROUND = new Color(211, 218, 224);
    private static final int ARC = 12;
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    private final Color hoverBackground;
    private final Color pressedBackground;

    public RoundedIconButton(String iconPath, String tooltip) {
        this(iconPath, tooltip, 18, 32, DEFAULT_HOVER_BACKGROUND, DEFAULT_PRESSED_BACKGROUND);
    }

    public RoundedIconButton(
            String iconPath,
            String tooltip,
            int iconSize,
            int buttonSize,
            Color hoverBackground,
            Color pressedBackground
    ) {
        this.hoverBackground = hoverBackground;
        this.pressedBackground = pressedBackground;
        setIcon(new FlatSVGIcon(iconPath, iconSize, iconSize));
        setToolTipText(tooltip);
        setText(null);
        setFocusable(false);
        setRequestFocusEnabled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(HAND_CURSOR);
        setPreferredSize(new Dimension(buttonSize, buttonSize));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(enabled ? HAND_CURSOR : DEFAULT_CURSOR);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color background = resolveBackground();
            if (background != null) {
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            }
        } finally {
            g2.dispose();
        }

        super.paintComponent(graphics);
    }

    private Color resolveBackground() {
        if (!isEnabled()) {
            return null;
        }
        if (getModel().isPressed()) {
            return pressedBackground;
        }
        if (getModel().isRollover()) {
            return hoverBackground;
        }
        return null;
    }
}
