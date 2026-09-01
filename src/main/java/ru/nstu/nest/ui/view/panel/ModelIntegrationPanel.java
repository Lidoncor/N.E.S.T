package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.ui.presenter.actions.ModelIntegrationPanelActions;
import ru.nstu.nest.ui.view.component.ModelIntegrationRow;
import ru.nstu.nest.ui.view.component.RoundedIconButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

@Component
public class ModelIntegrationPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final String EMPTY_TEXT = "Модели не настроены";

    private final JButton addButton = new RoundedIconButton("icons/add_icon.svg", "Добавить модель");
    private final JPanel contentPanel = new JPanel();

    private ModelIntegrationPanelActions actions;

    public ModelIntegrationPanel() {
        initUi();

        showModels(List.of());
    }

    private void initUi() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(true);
        setBackground(PANEL_BACKGROUND);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        contentPanel.setOpaque(true);
        contentPanel.setBackground(PANEL_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);

        addButton.addActionListener(event -> actions.importExisting());
    }

    public void bind(ModelIntegrationPanelActions actions) {
        this.actions = actions;
    }

    public void showModels(List<ModelIntegrationDescriptor> descriptors) {
        contentPanel.removeAll();

        if (descriptors.isEmpty()) {
            contentPanel.add(createPlaceholder(EMPTY_TEXT));
        } else {
            for (ModelIntegrationDescriptor descriptor : descriptors) {
                contentPanel.add(new ModelIntegrationRow(descriptor, actions));
                contentPanel.add(Box.createVerticalStrut(6));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND);
        return scrollPane;
    }

    private JLabel createPlaceholder(String text) {
        JLabel placeholder = new JLabel(text);
        placeholder.setAlignmentX(LEFT_ALIGNMENT);
        placeholder.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        return placeholder;
    }

}
