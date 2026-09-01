package ru.nstu.nest.ui.view.component;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class RoundedCardPanel extends JPanel {

    private final int arc;

    public RoundedCardPanel(int arc) {
        this.arc = arc;

        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } finally {
            g2.dispose();
        }

        super.paintComponent(graphics);
    }

}
