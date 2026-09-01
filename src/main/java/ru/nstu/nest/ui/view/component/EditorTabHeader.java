package ru.nstu.nest.ui.view.component;

import ru.nstu.nest.ui.presenter.actions.EditorPanelActions;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;

public class EditorTabHeader extends JPanel {

    public EditorTabHeader(File file, String title, EditorPanelActions actions) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setOpaque(false);

        JLabel label = new JLabel(title);
        JButton closeButton = new RoundedIconButton(
                "icons/close_icon.svg",
                "Закрыть",
                11,
                24,
                new Color(214, 220, 228),
                new Color(202, 209, 218)
        );
        closeButton.addActionListener(event -> {
            if (actions != null) {
                actions.closeRequested(file);
            }
        });

        add(label);
        add(closeButton);
    }

}
