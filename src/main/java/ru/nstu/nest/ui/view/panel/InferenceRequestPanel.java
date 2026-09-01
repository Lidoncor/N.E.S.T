package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;
import ru.nstu.nest.files.dto.AttachmentDescriptor;
import ru.nstu.nest.files.service.attachment.AttachmentReference;
import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Derivation;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.KnowledgeBaseDerivationSource;
import ru.nstu.nest.inference.dto.ModelDerivationSource;
import ru.nstu.nest.inference.service.TargetFactResolution;
import ru.nstu.nest.inference.source.kb.metadata.FrameDefinition;
import ru.nstu.nest.inference.source.kb.metadata.KnowledgebaseMetadata;
import ru.nstu.nest.inference.source.kb.metadata.SlotDefinition;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;
import ru.nstu.nest.ui.model.InferenceRequestHistoryEntry;
import ru.nstu.nest.ui.model.KnowledgebaseSourceInputStatus;
import ru.nstu.nest.ui.presenter.actions.InferenceRequestPanelActions;
import ru.nstu.nest.ui.service.FrameFactMapper;
import ru.nstu.nest.ui.service.FrameInputValidator;
import ru.nstu.nest.ui.view.common.SwingDialogs;
import ru.nstu.nest.ui.view.component.RoundedCardPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InferenceRequestPanel extends JPanel {

    private static final String STOPPED_CARD = "stopped";
    private static final String RUNNING_CARD = "running";
    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(214, 220, 228);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color ACCEPTED_CARD_BACKGROUND = new Color(241, 245, 249);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color TEXT_COLOR = new Color(15, 23, 42);
    private static final int CARD_ARC = 12;
    private static final int SLOT_LABEL_WIDTH = 320;
    private static final int TAB_CONTENT_TOP_INSET = 8;
    private static final int TREE_TEXT_LIMIT = 110;
    private static final String ATTACHMENT_OPEN_LINK = "Открыть";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final FrameInputValidator inputValidator;
    private final FrameFactMapper factMapper;

    private final JTabbedPane rootTabs = new JTabbedPane();
    private final CardLayout inputCardLayout = new CardLayout();
    private final JPanel inputCardPanel = new JPanel(inputCardLayout);
    private final JTabbedPane inputTabs = new JTabbedPane();
    private final JPanel framesPanel = new WidthTrackingPanel();
    private final JPanel targetsPanel = new JPanel();
    private final JPanel defaultTargetsPanel = new WidthTrackingPanel();
    private final JLabel inputMessageLabel = new JLabel();
    private final JButton submitButton = new JButton("Отправить запрос");
    private final JButton clearButton = new JButton("Новый запрос");
    private final DefaultListModel<InferenceRequestHistoryEntry> historyModel = new DefaultListModel<>();
    private final JList<InferenceRequestHistoryEntry> historyList = new JList<>(historyModel);
    private final JTree historyTree = new JTree(new DefaultMutableTreeNode("История"));

    private final List<FrameCardPanel> frameCards = new ArrayList<>();
    private final Map<String, List<FrameCardPanel>> frameCardsBySource = new LinkedHashMap<>();
    private Map<String, String> attachmentNamesById = new LinkedHashMap<>();
    private List<KnowledgebaseMetadata> metadata = List.of();
    private Set<String> imageAttachmentFactKeys = Set.of();
    private InferenceRequestPanelActions actions;
    private boolean projectRunning;
    private boolean requestRunning;

    public InferenceRequestPanel(FrameInputValidator inputValidator, FrameFactMapper factMapper) {
        this.inputValidator = inputValidator;
        this.factMapper = factMapper;

        initUi();
        setProjectStopped();
    }

    public void bind(InferenceRequestPanelActions actions) {
        this.actions = actions;
    }

    public void setProjectStopped() {
        projectRunning = false;
        requestRunning = false;
        metadata = List.of();
        imageAttachmentFactKeys = Set.of();
        attachmentNamesById = new LinkedHashMap<>();
        frameCards.clear();
        frameCardsBySource.clear();
        framesPanel.removeAll();
        defaultTargetsPanel.removeAll();
        inputMessageLabel.setText("");
        inputCardLayout.show(inputCardPanel, STOPPED_CARD);
        notifySourceStatuses();
    }

    public void setMetadata(List<KnowledgebaseMetadata> metadata) {
        setMetadata(metadata, Set.of());
    }

    public void setMetadata(List<KnowledgebaseMetadata> metadata, Set<String> imageAttachmentFactAddresses) {
        setMetadata(metadata, imageAttachmentFactAddresses, List.of());
    }

    public void setMetadata(
            List<KnowledgebaseMetadata> metadata,
            Set<String> imageAttachmentFactAddresses,
            List<AttachmentDescriptor> attachments
    ) {
        this.metadata = List.copyOf(metadata);
        this.imageAttachmentFactKeys = imageAttachmentFactAddresses.stream()
                .map(Address::parse)
                .map(Address::key)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.attachmentNamesById = attachmentNamesById(attachments);
        projectRunning = true;
        requestRunning = false;
        rebuildInputFrames();
        rebuildTargets();
        inputCardLayout.show(inputCardPanel, RUNNING_CARD);
        refreshSubmitState();
    }

    private Map<String, String> attachmentNamesById(List<AttachmentDescriptor> attachments) {
        Map<String, String> result = new LinkedHashMap<>();
        for (AttachmentDescriptor attachment : attachments) {
            result.put(attachment.id(), attachment.name());
        }
        return result;
    }

    public void setRequestRunning(boolean requestRunning) {
        this.requestRunning = requestRunning;
        refreshSubmitState();
    }

    public void showRequestError(String message) {
        if (message == null || message.isBlank()) {
            message = "Не удалось выполнить запрос";
        }
        inputMessageLabel.setForeground(ERROR_COLOR);
        inputMessageLabel.setText(message);
        SwingDialogs.showError(this, message, "Запрос");
    }

    public void addHistoryEntry(InferenceRequestHistoryEntry entry) {
        historyModel.add(0, entry);
        historyList.setSelectedIndex(0);
        rootTabs.setSelectedIndex(1);
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(320, 0));
        setPreferredSize(new Dimension(360, 0));
        setBackground(PANEL_BACKGROUND);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        configureTabbedPane(rootTabs);
        inputCardPanel.setBackground(PANEL_BACKGROUND);
        configureInputTab();
        configureHistoryTab();

        rootTabs.addTab("Ввод", inputCardPanel);
        rootTabs.addTab("История", createHistoryPanel());
        add(rootTabs, BorderLayout.CENTER);
    }

    private void configureTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setOpaque(true);
        tabbedPane.setBackground(PANEL_BACKGROUND);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private void configureInputTab() {
        inputCardPanel.add(createStoppedPanel(), STOPPED_CARD);
        inputCardPanel.add(createRunningInputPanel(), RUNNING_CARD);
    }

    private JPanel createStoppedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);

        JLabel label = new JLabel("Сначала запустите проект", JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRunningInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        configureTabbedPane(inputTabs);

        framesPanel.setLayout(new BoxLayout(framesPanel, BoxLayout.Y_AXIS));
        framesPanel.setBackground(PANEL_BACKGROUND);
        framesPanel.setBorder(BorderFactory.createEmptyBorder(TAB_CONTENT_TOP_INSET, 0, 0, 0));

        targetsPanel.setLayout(new BorderLayout(0, 8));
        targetsPanel.setBackground(PANEL_BACKGROUND);
        defaultTargetsPanel.setLayout(new BoxLayout(defaultTargetsPanel, BoxLayout.Y_AXIS));
        defaultTargetsPanel.setBackground(PANEL_BACKGROUND);

        JPanel targetContentPanel = new JPanel(new BorderLayout(0, 8));
        targetContentPanel.setBackground(PANEL_BACKGROUND);
        targetContentPanel.setBorder(BorderFactory.createEmptyBorder(TAB_CONTENT_TOP_INSET, 0, 0, 0));
        targetContentPanel.add(defaultTargetsPanel, BorderLayout.NORTH);
        targetsPanel.add(wrapScroll(targetContentPanel), BorderLayout.CENTER);

        inputTabs.addTab("Исходные факты", wrapScroll(framesPanel));
        inputTabs.addTab("Целевые факты", targetsPanel);

        panel.add(inputTabs, BorderLayout.CENTER);
        panel.add(createInputButtonsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInputButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        inputMessageLabel.setForeground(ERROR_COLOR);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        clearButton.addActionListener(_ -> clearInput());
        submitButton.addActionListener(_ -> submitRequest());

        buttons.add(clearButton);
        buttons.add(submitButton);

        panel.add(inputMessageLabel, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private void configureHistoryTab() {
        historyList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText("Запрос " + value.number() + " - " + TIME_FORMAT.format(value.timestamp()));
            }
            return label;
        });
        historyList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showHistoryEntry(historyList.getSelectedValue());
            }
        });
        historyTree.setCellRenderer(new AttachmentTreeCellRenderer());
        historyTree.setRootVisible(true);
        ToolTipManager.sharedInstance().registerComponent(historyTree);
        historyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleHistoryTreeClick(event);
            }
        });
        historyTree.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                historyTree.setCursor(isAttachmentLinkAt(event.getPoint())
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setOpaque(true);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createHistoryCard("Запросы", historyList),
                createHistoryCard("Детали", historyTree)
        );
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(8);
        splitPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        splitPane.setBackground(PANEL_BACKGROUND);
        splitPane.setOpaque(true);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane wrapScroll(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND);
        return scrollPane;
    }

    private JPanel createHistoryCard(String title, JComponent component) {
        RoundedCardPanel card = new RoundedCardPanel(CARD_ARC);
        card.setLayout(new BorderLayout(0, 6));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void rebuildInputFrames() {
        frameCards.clear();
        frameCardsBySource.clear();
        framesPanel.removeAll();

        for (KnowledgebaseMetadata sourceMetadata : metadata) {
            if (sourceMetadata.inputFrames().isEmpty()) {
                continue;
            }

            JPanel sourcePanel = createSourceFramesPanel(sourceMetadata.sourceName());
            List<FrameCardPanel> sourceCards = new ArrayList<>();
            for (FrameDefinition frame : sourceMetadata.inputFrames()) {
                FrameCardPanel card = new FrameCardPanel(frame);
                frameCards.add(card);
                sourceCards.add(card);
                sourcePanel.add(card);
                sourcePanel.add(Box.createVerticalStrut(8));
            }
            frameCardsBySource.put(sourceMetadata.sourceName(), sourceCards);
            framesPanel.add(sourcePanel);
            framesPanel.add(Box.createVerticalStrut(12));
        }

        if (frameCards.isEmpty()) {
            JLabel placeholder = new JLabel("В базах знаний не описаны входные фреймы");
            placeholder.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
            framesPanel.add(placeholder);
        }

        framesPanel.revalidate();
        framesPanel.repaint();
    }

    private JPanel createSourceFramesPanel(String sourceName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("База знаний: " + sourceName);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        return panel;
    }

    private void rebuildTargets() {
        defaultTargetsPanel.removeAll();

        boolean hasDefaultTargets = false;
        for (KnowledgebaseMetadata sourceMetadata : metadata) {
            if (sourceMetadata.targetFacts().isEmpty()) {
                continue;
            }
            hasDefaultTargets = true;
            defaultTargetsPanel.add(createSourceTargetsPanel(sourceMetadata));
            defaultTargetsPanel.add(Box.createVerticalStrut(8));
        }

        if (!hasDefaultTargets) {
            JLabel placeholder = new JLabel("Целевые факты не описаны");
            placeholder.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
            defaultTargetsPanel.add(placeholder);
        }

        defaultTargetsPanel.revalidate();
        defaultTargetsPanel.repaint();
    }

    private JPanel createSourceTargetsPanel(KnowledgebaseMetadata sourceMetadata) {
        RoundedCardPanel card = new RoundedCardPanel(CARD_ARC);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Цели из базы знаний: " + sourceMetadata.sourceName());
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        for (TargetFactDefinition target : sourceMetadata.targetFacts()) {
            JLabel targetLabel = new JLabel(target.displayName());
            targetLabel.setAlignmentX(LEFT_ALIGNMENT);
            card.add(targetLabel);
        }

        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private void clearInput() {
        for (FrameCardPanel card : frameCards) {
            card.clear();
        }
        inputMessageLabel.setText("");
        refreshSubmitState();
    }

    private void submitRequest() {
        inputMessageLabel.setText("");
        List<Fact> inputFacts = frameCards.stream()
                .flatMap(card -> card.toFacts().stream())
                .toList();
        actions.submitRequested(inputFacts, collectTargets());
    }

    private List<TargetFactDefinition> collectTargets() {
        Map<String, TargetFactDefinition> targetsByAddress = new LinkedHashMap<>();
        metadata.stream()
                .flatMap(sourceMetadata -> sourceMetadata.targetFacts().stream())
                .forEach(target -> targetsByAddress.putIfAbsent(target.address().key(), target));

        return targetsByAddress.values().stream().toList();
    }

    private void refreshSubmitState() {
        boolean hasInputFrames = !frameCards.isEmpty();
        boolean allAccepted = hasInputFrames && frameCards.stream().allMatch(FrameCardPanel::isAccepted);

        submitButton.setEnabled(projectRunning && !requestRunning && allAccepted);
        clearButton.setEnabled(projectRunning && !requestRunning && hasInputFrames);

        if (!hasInputFrames) {
            inputMessageLabel.setForeground(ERROR_COLOR);
            inputMessageLabel.setText("В базах знаний не описаны входные фреймы");
        } else if (!requestRunning && allAccepted) {
            inputMessageLabel.setText("");
        }

        notifySourceStatuses();
    }

    private void notifySourceStatuses() {
        if (actions != null) {
            actions.sourceStatusesChanged(buildSourceStatuses());
        }
    }

    private Map<String, KnowledgebaseSourceInputStatus> buildSourceStatuses() {
        Map<String, KnowledgebaseSourceInputStatus> result = new LinkedHashMap<>();

        for (KnowledgebaseMetadata sourceMetadata : metadata) {
            List<FrameCardPanel> sourceCards = frameCardsBySource.getOrDefault(sourceMetadata.sourceName(), List.of());
            if (sourceCards.isEmpty()) {
                result.put(sourceMetadata.sourceName(), KnowledgebaseSourceInputStatus.NONE);
                continue;
            }

            boolean accepted = sourceCards.stream().allMatch(FrameCardPanel::isAccepted);
            result.put(
                    sourceMetadata.sourceName(),
                    accepted ? KnowledgebaseSourceInputStatus.COMPLETE : KnowledgebaseSourceInputStatus.INCOMPLETE
            );
        }

        return result;
    }

    private void showHistoryEntry(InferenceRequestHistoryEntry entry) {
        if (entry == null) {
            historyTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("История")));
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                "Запрос " + entry.number() + " - " + TIME_FORMAT.format(entry.timestamp())
        );
        root.add(createInputFactsNode(entry.inputFacts()));
        root.add(createTargetFactsNode(entry.targetResolutions()));
        root.add(createDerivationNode(entry.responseFacts()));

        historyTree.setModel(new DefaultTreeModel(root));
        expandHistoryTree();
    }

    private DefaultMutableTreeNode createInputFactsNode(List<Fact> facts) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Входные факты");
        facts.stream()
                .map(FactTreeItem::new)
                .map(DefaultMutableTreeNode::new)
                .forEach(node::add);
        return node;
    }

    private DefaultMutableTreeNode createTargetFactsNode(List<TargetFactResolution> resolutions) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Целевые факты");
        if (resolutions.isEmpty()) {
            node.add(new DefaultMutableTreeNode("Целевые факты не заданы"));
            return node;
        }

        for (TargetFactResolution resolution : resolutions) {
            if (resolution.found()) {
                node.add(new DefaultMutableTreeNode(
                        resolution.target().displayName() + ": " + formatTargetValues(resolution.values())
                ));
                continue;
            }
            node.add(new DefaultMutableTreeNode(resolution.target().displayName() + " - Цель не найдена"));
        }
        return node;
    }

    private String formatTargetValues(List<String> values) {
        return values.stream()
                .map(this::displayFactValue)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private DefaultMutableTreeNode createDerivationNode(List<Fact> responseFacts) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Ход вывода");
        if (responseFacts.isEmpty()) {
            node.add(new DefaultMutableTreeNode("Результаты вывода отсутствуют"));
            return node;
        }

        responseFacts.stream()
                .map(this::createFactTreeNode)
                .forEach(node::add);
        return node;
    }

    private DefaultMutableTreeNode createFactTreeNode(Fact fact) {
        DefaultMutableTreeNode factNode = new DefaultMutableTreeNode(new FactTreeItem(fact));
        for (Derivation derivation : fact.derivationHistory()) {
            DefaultMutableTreeNode derivationNode = new DefaultMutableTreeNode(formatDerivation(derivation));
            derivation.supportingFacts().stream()
                    .map(this::createFactTreeNode)
                    .forEach(derivationNode::add);
            factNode.add(derivationNode);
        }
        return factNode;
    }

    private String formatDerivation(Derivation derivation) {
        if (derivation.source() instanceof KnowledgeBaseDerivationSource source) {
            return "БАЗА ЗНАНИЙ: " + source.id() + " / " + source.originId();
        }
        if (derivation.source() instanceof ModelDerivationSource source) {
            return "МОДЕЛЬ: " + source.id();
        }
        return derivation.source().id();
    }

    private String formatFact(Fact fact) {
        return fact.address().value() + " = " + displayFactValue(fact.value());
    }

    private String displayFactValue(String value) {
        return AttachmentReference.parseId(value)
                .map(id -> attachmentNamesById.getOrDefault(id, id))
                .orElse(value);
    }

    private void handleHistoryTreeClick(MouseEvent event) {
        if (actions == null) {
            return;
        }

        TreePath path = historyTree.getPathForLocation(event.getX(), event.getY());
        if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) {
            return;
        }

        if (!isAttachmentLinkAt(event.getPoint())) {
            return;
        }

        Object userObject = node.getUserObject();
        if (userObject instanceof FactTreeItem item) {
            actions.previewAttachmentRequested(item.fact().value());
        }
    }

    private boolean isAttachmentLinkAt(Point point) {
        TreePath path = historyTree.getPathForLocation(point.x, point.y);
        if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) {
            return false;
        }

        Object userObject = node.getUserObject();
        if (!(userObject instanceof FactTreeItem item) || !AttachmentReference.isReference(item.fact().value())) {
            return false;
        }

        Rectangle bounds = historyTree.getPathBounds(path);
        if (bounds == null) {
            return false;
        }

        int linkStartX = attachmentLinkStartX(bounds, item.fact());
        int linkEndX = linkStartX + linkWidth(historyTree.getFontMetrics(historyTree.getFont()));
        return point.x >= linkStartX && point.x <= linkEndX && point.y >= bounds.y && point.y <= bounds.y + bounds.height;
    }

    private void expandHistoryTree() {
        for (int row = 0; row < historyTree.getRowCount(); row++) {
            historyTree.expandRow(row);
        }
    }

    private final class FrameCardPanel extends RoundedCardPanel {

        private final FrameDefinition frame;
        private final Map<String, JComponent> fields = new LinkedHashMap<>();
        private final JButton editButton = new JButton("Редактировать");
        private final JLabel errorLabel = new JLabel();
        private boolean accepted;
        private boolean suppressFieldEvents;

        private FrameCardPanel(FrameDefinition frame) {
            super(CARD_ARC);
            this.frame = frame;

            initCard();
        }

        private boolean isAccepted() {
            return accepted;
        }

        private void initCard() {
            setLayout(new BorderLayout(0, 8));
            setBackground(CARD_BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setAlignmentX(LEFT_ALIGNMENT);

            add(createHeader(), BorderLayout.NORTH);
            add(createFieldsPanel(), BorderLayout.CENTER);
            add(createFooter(), BorderLayout.SOUTH);

            editButton.setEnabled(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }

        private JPanel createHeader() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel frameLabel = new JLabel(frame.name());
            frameLabel.setFont(frameLabel.getFont().deriveFont(Font.BOLD));
            frameLabel.setAlignmentX(LEFT_ALIGNMENT);

            panel.add(frameLabel);
            return panel;
        }

        private JPanel createFieldsPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            for (SlotDefinition slot : frame.slots()) {
                JComponent component = createField(slot);
                fields.put(slot.name(), component);
                panel.add(createFieldBlock(slot, component));
                panel.add(Box.createVerticalStrut(8));
            }

            return panel;
        }

        private JPanel createFieldBlock(SlotDefinition slot, JComponent component) {
            JPanel block = new JPanel(new BorderLayout(0, 4));
            block.setOpaque(false);
            block.setAlignmentX(LEFT_ALIGNMENT);

            JLabel label = createSlotLabel(slot);
            label.setToolTipText(slot.question());

            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));

            block.add(label, BorderLayout.NORTH);
            block.add(component, BorderLayout.CENTER);
            block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
            return block;
        }

        private JComponent createField(SlotDefinition slot) {
            if (!slot.options().isEmpty()) {
                JComboBox<String> comboBox = new JComboBox<>(slot.options().toArray(String[]::new));
                comboBox.setSelectedIndex(-1);
                comboBox.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMM");
                comboBox.setToolTipText(slot.question());
                comboBox.addActionListener(_ -> fieldChanged());
                return comboBox;
            }

            if (isImageAttachmentSlot(slot)) {
                return new AttachmentValueField(slot.question());
            }

            return createTextField(slot);
        }

        private JTextField createTextField(SlotDefinition slot) {
            JTextField field = new JTextField();
            field.setToolTipText(slot.question());
            field.addActionListener(_ -> fieldChanged());
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent event) {
                    fieldChanged();
                }
            });
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    markEditing();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    markEditing();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    markEditing();
                }
            });
            return field;
        }

        private boolean isImageAttachmentSlot(SlotDefinition slot) {
            return imageAttachmentFactKeys.contains(Address.of(frame.name(), slot.name()).key());
        }

        private JLabel createSlotLabel(SlotDefinition slot) {
            JLabel label = new JLabel("<html><body style='width: " + SLOT_LABEL_WIDTH + "px'>"
                    + escapeHtml(slot.label()) + "</body></html>");
            label.setPreferredSize(new Dimension(SLOT_LABEL_WIDTH, label.getPreferredSize().height));
            return label;
        }

        private JPanel createFooter() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);
            errorLabel.setForeground(ERROR_COLOR);

            editButton.addActionListener(_ -> handleEditButton());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(editButton);

            panel.add(errorLabel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.EAST);
            return panel;
        }

        private void fieldChanged() {
            if (suppressFieldEvents || accepted) {
                return;
            }

            List<String> errors = inputValidator.validate(frame, valuesBySlotName());
            if (!errors.isEmpty()) {
                errorLabel.setText(errors.contains(FrameInputValidator.NUMERIC_FIELD_ERROR)
                        ? FrameInputValidator.NUMERIC_FIELD_ERROR
                        : "");
                setAccepted(false);
                return;
            }

            errorLabel.setText("");
            setAccepted(true);
        }

        private void markEditing() {
            if (suppressFieldEvents || accepted) {
                return;
            }

            errorLabel.setText("");
            editButton.setEnabled(hasValidValues());
            refreshSubmitState();
        }

        private void setAccepted(boolean accepted) {
            this.accepted = accepted;
            fields.values().forEach(field -> field.setEnabled(!accepted));
            editButton.setEnabled(accepted || hasValidValues());
            editButton.setText(accepted ? "Редактировать" : "Принять");
            setBackground(accepted ? ACCEPTED_CARD_BACKGROUND : CARD_BACKGROUND);
            repaint();
            refreshSubmitState();
        }

        private void handleEditButton() {
            errorLabel.setText("");
            if (accepted) {
                setAccepted(false);
                return;
            }

            if (hasValidValues()) {
                setAccepted(true);
            }
        }

        private void clear() {
            suppressFieldEvents = true;
            for (JComponent field : fields.values()) {
                if (field instanceof JTextField textField) {
                    textField.setText("");
                } else if (field instanceof AttachmentValueField attachmentField) {
                    attachmentField.clearValue();
                } else if (field instanceof JComboBox<?> comboBox) {
                    comboBox.setSelectedIndex(-1);
                }
            }
            suppressFieldEvents = false;
            errorLabel.setText("");
            setAccepted(false);
        }

        private List<Fact> toFacts() {
            return factMapper.toFacts(frame, valuesBySlotName());
        }

        private Map<String, String> valuesBySlotName() {
            Map<String, String> values = new LinkedHashMap<>();
            fields.forEach((slotName, field) -> values.put(slotName, valueOf(field)));
            return values;
        }

        private String valueOf(JComponent field) {
            if (field instanceof JTextField textField) {
                return textField.getText();
            }
            if (field instanceof AttachmentValueField attachmentField) {
                return attachmentField.value();
            }
            if (field instanceof JComboBox<?> comboBox) {
                Object selected = comboBox.getSelectedItem();
                return selected == null ? "" : selected.toString();
            }
            return "";
        }

        private boolean hasValidValues() {
            return inputValidator.validate(frame, valuesBySlotName()).isEmpty();
        }

        private final class AttachmentValueField extends JPanel {

            private final JTextField textField = new JTextField();
            private final JButton attachButton = new JButton("Изображение...");
            private final JButton openButton = new JButton("Открыть");
            private String attachmentValue;
            private boolean suppressEvents;

            private AttachmentValueField(String tooltip) {
                setLayout(new BorderLayout(6, 0));
                setOpaque(false);

                textField.setToolTipText(tooltip);
                textField.addActionListener(_ -> fieldChanged());
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent event) {
                        fieldChanged();
                    }
                });
                textField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        handleTextEdited();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        handleTextEdited();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        handleTextEdited();
                    }
                });

                attachButton.addActionListener(_ -> chooseImage());
                openButton.addActionListener(_ -> previewAttachment());

                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                buttons.setOpaque(false);
                buttons.add(openButton);
                buttons.add(attachButton);

                add(textField, BorderLayout.CENTER);
                add(buttons, BorderLayout.EAST);
                updateOpenButton();
            }

            private String value() {
                if (attachmentValue != null && !attachmentValue.isBlank()) {
                    return attachmentValue;
                }
                return textField.getText();
            }

            private void clearValue() {
                suppressEvents = true;
                attachmentValue = null;
                textField.setText("");
                textField.setToolTipText(null);
                suppressEvents = false;
                updateOpenButton();
            }

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                textField.setEnabled(enabled);
                attachButton.setEnabled(enabled);
                openButton.setEnabled(AttachmentReference.isReference(value()));
            }

            private void chooseImage() {
                if (actions == null) {
                    return;
                }

                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter(
                        "Изображения",
                        "png",
                        "jpg",
                        "jpeg",
                        "gif",
                        "bmp",
                        "webp"
                ));
                if (chooser.showOpenDialog(InferenceRequestPanel.this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                try {
                    AttachmentDescriptor descriptor = actions.createImageAttachment(chooser.getSelectedFile().toPath());
                    setAttachment(descriptor);
                    fieldChanged();
                } catch (RuntimeException e) {
                    showRequestError(e.getMessage());
                }
            }

            private void setAttachment(AttachmentDescriptor descriptor) {
                suppressEvents = true;
                attachmentNamesById.put(descriptor.id(), descriptor.name());
                attachmentValue = AttachmentReference.format(descriptor.id());
                textField.setText(descriptor.name());
                textField.setToolTipText(attachmentValue);
                suppressEvents = false;
                updateOpenButton();
            }

            private void previewAttachment() {
                if (actions != null && AttachmentReference.isReference(value())) {
                    actions.previewAttachmentRequested(value());
                }
            }

            private void handleTextEdited() {
                if (suppressEvents) {
                    return;
                }
                attachmentValue = null;
                updateOpenButton();
                markEditing();
            }

            private void updateOpenButton() {
                openButton.setEnabled(AttachmentReference.isReference(value()));
            }
        }

    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, Math.max(0, limit - 3)) + "...";
    }

    private final class AttachmentTreeCellRenderer extends JLabel implements TreeCellRenderer {

        private final DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

        private AttachmentTreeCellRenderer() {
            defaultRenderer.setLeafIcon(null);
            defaultRenderer.setOpenIcon(null);
            defaultRenderer.setClosedIcon(null);
        }

        @Override
        public java.awt.Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus
        ) {
            JLabel label = (JLabel) defaultRenderer.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus
            );
            String text = displayText(value);
            label.setIcon(null);
            label.setText(renderTreeText(value, text));
            label.setToolTipText(text);
            return label;
        }

    }

    private boolean isAttachmentNode(Object value) {
        Object userObject = userObject(value);
        return userObject instanceof FactTreeItem item && AttachmentReference.isReference(item.fact().value());
    }

    private String displayText(Object value) {
        Object userObject = userObject(value);
        if (userObject instanceof FactTreeItem item) {
            return formatFact(item.fact());
        }
        return String.valueOf(value);
    }

    private String renderTreeText(Object value, String text) {
        if (!isAttachmentNode(value)) {
            return escapeHtml(truncate(text, TREE_TEXT_LIMIT));
        }

        return "<html>"
                + escapeHtml(truncate(text, TREE_TEXT_LIMIT))
                + " <span style='color:#2563eb'><u>"
                + ATTACHMENT_OPEN_LINK
                + "</u></span>"
                + "</html>";
    }

    private int attachmentLinkStartX(Rectangle bounds, Fact fact) {
        FontMetrics metrics = historyTree.getFontMetrics(historyTree.getFont());
        return bounds.x + metrics.stringWidth(truncate(formatFact(fact), TREE_TEXT_LIMIT)) + metrics.charWidth(' ');
    }

    private int linkWidth(FontMetrics metrics) {
        return metrics.stringWidth(ATTACHMENT_OPEN_LINK);
    }

    private Object userObject(Object value) {
        if (value instanceof DefaultMutableTreeNode node) {
            return node.getUserObject();
        }
        return value;
    }

    private record FactTreeItem(Fact fact) {

        @Override
        public String toString() {
            return fact.address().value() + " = " + fact.value();
        }
    }

    private static final class WidthTrackingPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    }

}
