package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Component
public class WorkspacePanel extends JPanel {

    private static final int DEFAULT_SIDEBAR_WIDTH = 320;
    private static final int DEFAULT_REQUEST_PANEL_WIDTH = 460;
    private static final int MIN_SIDEBAR_WIDTH = 260;
    private static final int MIN_EDITOR_WIDTH = 360;
    private static final int MIN_REQUEST_PANEL_WIDTH = 420;
    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final String WELCOME_CARD = "welcome";
    private static final String WORKSPACE_CARD = "workspace";

    private final CardLayout cardLayout = new CardLayout();
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel emptyLeftPanel = createEmptyPanel(new Dimension(MIN_SIDEBAR_WIDTH, 0));
    private final JPanel emptyRightPanel = createEmptyPanel(null);
    private final JSplitPane editorAreaSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private final EditorPanel editorPanel;
    private final InferenceRequestPanel inferenceRequestPanel;
    private final ProjectSidebarPanel projectSidebarPanel;
    private boolean editorAreaDividerInitialized;

    public WorkspacePanel(
            EditorPanel editorPanel,
            InferenceRequestPanel inferenceRequestPanel,
            ProjectSidebarPanel projectSidebarPanel
    ) {
        this.editorPanel = editorPanel;
        this.inferenceRequestPanel = inferenceRequestPanel;
        this.projectSidebarPanel = projectSidebarPanel;

        initUi();
    }

    private void initUi() {
        setLayout(cardLayout);
        setBackground(PANEL_BACKGROUND);

        configureSplitPane();
        configureEditorAreaSplitPane();
        add(new WelcomePanel(), WELCOME_CARD);
        add(splitPane, WORKSPACE_CARD);

        projectSidebarPanel.setMinimumSize(new Dimension(MIN_SIDEBAR_WIDTH, 0));

        cardLayout.show(this, WELCOME_CARD);
    }

    public void enableProjectSidebar() {
        setLeftComponent(projectSidebarPanel);
    }

    public void enableEditor() {
        if (splitPane.getRightComponent() == editorAreaSplitPane) {
            updateVisibleCard();
            return;
        }

        editorAreaDividerInitialized = false;
        setRightComponent(editorAreaSplitPane);
        SwingUtilities.invokeLater(this::resetEditorAreaDivider);
    }

    private void configureSplitPane() {
        splitPane.setDividerSize(0);
        splitPane.setFocusable(false);
        splitPane.setRequestFocusEnabled(false);
        splitPane.setResizeWeight(0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setBackground(PANEL_BACKGROUND);
        splitPane.setDividerLocation(DEFAULT_SIDEBAR_WIDTH);

        splitPane.setLeftComponent(emptyLeftPanel);
        splitPane.setRightComponent(emptyRightPanel);
    }

    private void configureEditorAreaSplitPane() {
        editorAreaSplitPane.setDividerSize(6);
        editorAreaSplitPane.setFocusable(false);
        editorAreaSplitPane.setRequestFocusEnabled(false);
        editorAreaSplitPane.setResizeWeight(1);
        editorAreaSplitPane.setBorder(BorderFactory.createEmptyBorder());
        editorAreaSplitPane.setBackground(PANEL_BACKGROUND);

        editorPanel.setMinimumSize(new Dimension(360, 0));
        inferenceRequestPanel.setMinimumSize(new Dimension(MIN_REQUEST_PANEL_WIDTH, 0));
        inferenceRequestPanel.setPreferredSize(new Dimension(DEFAULT_REQUEST_PANEL_WIDTH, 0));

        editorAreaSplitPane.setLeftComponent(editorPanel);
        editorAreaSplitPane.setRightComponent(inferenceRequestPanel);
        editorAreaSplitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (!editorAreaDividerInitialized) {
                    resetEditorAreaDivider();
                }
            }
        });
    }

    private void resetEditorAreaDivider() {
        int width = editorAreaSplitPane.getWidth();
        if (width <= 0) {
            return;
        }

        editorAreaSplitPane.setDividerLocation(Math.max(MIN_EDITOR_WIDTH, width - DEFAULT_REQUEST_PANEL_WIDTH));
        editorAreaDividerInitialized = true;
    }

    private JPanel createEmptyPanel(Dimension minimumSize) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BACKGROUND);
        if (minimumSize != null) {
            panel.setMinimumSize(minimumSize);
        }

        return panel;
    }

    private void setLeftComponent(java.awt.Component component) {
        splitPane.setLeftComponent(component);
        splitPane.setDividerLocation(DEFAULT_SIDEBAR_WIDTH);

        updateVisibleCard();
    }

    private void setRightComponent(java.awt.Component component) {
        splitPane.setRightComponent(component);
        splitPane.setDividerLocation(DEFAULT_SIDEBAR_WIDTH);

        updateVisibleCard();
    }

    private void updateVisibleCard() {
        cardLayout.show(this, hasWorkspaceContent() ? WORKSPACE_CARD : WELCOME_CARD);
    }

    private boolean hasWorkspaceContent() {
        return splitPane.getLeftComponent() == projectSidebarPanel
                || splitPane.getRightComponent() == editorAreaSplitPane;
    }

}
