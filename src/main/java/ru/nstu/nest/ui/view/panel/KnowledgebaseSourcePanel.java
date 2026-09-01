package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.ui.presenter.actions.KnowledgebaseSourcePanelActions;
import ru.nstu.nest.ui.model.KnowledgebaseSourceInputStatus;
import ru.nstu.nest.ui.view.component.KnowledgebaseSourceRow;
import ru.nstu.nest.ui.view.component.RoundedIconButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgebaseSourcePanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final String EMPTY_TEXT = "Базы знаний не настроены";

    private final JButton addButton = new RoundedIconButton("icons/add_icon.svg", "Добавить базу знаний");
    private final JPanel contentPanel = new JPanel();
    private final JPopupMenu addMenu = new JPopupMenu();
    private final Map<String, KnowledgebaseSourceRow> rows = new LinkedHashMap<>();
    private Map<String, KnowledgebaseSourceInputStatus> sourceStatuses = new HashMap<>();

    private KnowledgebaseSourcePanelActions actions;

    public KnowledgebaseSourcePanel() {
        initUi();
        showSources(List.of());
    }

    private void initUi() {
        setOpaque(true);
        setBackground(PANEL_BACKGROUND);
        setLayout(new BorderLayout(0, 8));

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        contentPanel.setOpaque(true);
        contentPanel.setBackground(PANEL_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);
        configureAddMenu();
    }

    public void bind(KnowledgebaseSourcePanelActions actions) {
        this.actions = actions;
    }

    public void showSources(List<KnowledgebaseSourceDescriptor> descriptors) {
        contentPanel.removeAll();
        rows.clear();

        if (descriptors.isEmpty()) {
            contentPanel.add(createPlaceholder());
        } else {
            for (KnowledgebaseSourceDescriptor descriptor : descriptors) {
                KnowledgebaseSourceRow row = new KnowledgebaseSourceRow(descriptor, actions);
                row.setInputStatus(sourceStatuses.getOrDefault(
                        descriptor.name(),
                        KnowledgebaseSourceInputStatus.NONE
                ));
                rows.put(descriptor.name(), row);
                contentPanel.add(row);
                contentPanel.add(Box.createVerticalStrut(6));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setSourceStatuses(Map<String, KnowledgebaseSourceInputStatus> sourceStatuses) {
        this.sourceStatuses = new HashMap<>(sourceStatuses);
        rows.forEach((sourceName, row) -> row.setInputStatus(this.sourceStatuses.getOrDefault(
                sourceName,
                KnowledgebaseSourceInputStatus.NONE
        )));
    }

    public void clearSourceStatuses() {
        sourceStatuses = new HashMap<>();
        rows.values().forEach(row -> row.setInputStatus(KnowledgebaseSourceInputStatus.NONE));
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

    private void configureAddMenu() {
        JMenuItem createItem = new JMenuItem("Создать");
        createItem.addActionListener(event -> actions.create());
        addMenu.add(createItem);

        JMenuItem importItem = new JMenuItem("Импортировать");
        importItem.addActionListener(event -> actions.importExisting());
        addMenu.add(importItem);

        addButton.addActionListener(event -> addMenu.show(addButton, 0, addButton.getHeight()));
    }

    private JLabel createPlaceholder() {
        JLabel placeholder = new JLabel(KnowledgebaseSourcePanel.EMPTY_TEXT);
        placeholder.setAlignmentX(LEFT_ALIGNMENT);
        placeholder.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));

        return placeholder;
    }

}
