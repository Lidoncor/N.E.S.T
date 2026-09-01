package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;
import ru.nstu.nest.ui.view.component.RoundedIconButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.function.Consumer;

@Component
public class ProjectRunStatusPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(214, 220, 228);
    private static final Color STATUS_IDLE = new Color(148, 163, 184);
    private static final Color STATUS_RUNNING = new Color(40, 167, 69);

    private final JLabel statusDotLabel = new JLabel("●");
    private final JLabel statusTextLabel = new JLabel("Запущен");
    private final JButton settingsButton = new RoundedIconButton("icons/settings_icon.svg", "Настройки проекта");
    private final JButton runButton = new JButton("Запустить");
    private final JButton stopButton = new JButton("Остановить");

    public ProjectRunStatusPanel() {
        initUi();

        setProjectClosed();
    }

    private void initUi() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        setPreferredSize(new Dimension(0, 40));
        setBackground(PANEL_BACKGROUND);

        runButton.setFocusable(false);
        runButton.setRequestFocusEnabled(false);
        stopButton.setFocusable(false);
        stopButton.setRequestFocusEnabled(false);

        JPanel leftSection = new JPanel();
        leftSection.setOpaque(false);
        leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.X_AXIS));
        leftSection.add(settingsButton);

        JPanel rightSection = new JPanel();
        rightSection.setOpaque(false);
        rightSection.setLayout(new BoxLayout(rightSection, BoxLayout.X_AXIS));
        rightSection.add(statusDotLabel);
        rightSection.add(Box.createHorizontalStrut(6));
        rightSection.add(statusTextLabel);
        rightSection.add(Box.createHorizontalStrut(16));
        rightSection.add(runButton);
        rightSection.add(Box.createHorizontalStrut(6));
        rightSection.add(stopButton);

        add(leftSection, BorderLayout.WEST);
        add(rightSection, BorderLayout.EAST);
    }

    public void onRun(Consumer<JFrame> action) {
        runButton.addActionListener(event -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            action.accept(parent);
        });
    }

    public void onStop(Runnable action) {
        stopButton.addActionListener(_ -> action.run());
    }

    public void onSettings(Consumer<JFrame> action) {
        settingsButton.addActionListener(event -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            action.accept(parent);
        });
    }

    public void setProjectClosed() {
        applyState(false, false);
    }

    public void setProjectReady() {
        applyState(true, false);
    }

    public void setRunning() {
        applyState(true, true);
    }

    public void setStarting() {
        statusDotLabel.setForeground(STATUS_IDLE);
        statusTextLabel.setVisible(false);
        settingsButton.setEnabled(false);
        runButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void applyState(boolean projectOpened, boolean running) {
        statusDotLabel.setForeground(running ? STATUS_RUNNING : STATUS_IDLE);
        statusTextLabel.setVisible(running);

        settingsButton.setEnabled(projectOpened && !running);
        runButton.setEnabled(projectOpened && !running);
        stopButton.setEnabled(projectOpened && running);
    }

}
