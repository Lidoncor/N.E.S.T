package ru.nstu.nest.ui.view.panel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;

public class WelcomePanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final Color SUBTITLE_COLOR = new Color(100, 116, 139);

    public WelcomePanel() {
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BACKGROUND);
        add(createContentWrapper(), BorderLayout.CENTER);
    }

    private JPanel createContentWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(createContent());

        return wrapper;
    }

    private JPanel createContent() {
        JLabel title = new JLabel("Откройте или создайте проект", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html><div style='text-align:center;'>Создайте новый проект или откройте существующий с базами знаний и моделями.</div></html>",
                SwingConstants.CENTER
        );
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(SUBTITLE_COLOR);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(subtitle);

        return content;
    }

}
