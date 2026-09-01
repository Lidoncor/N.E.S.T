package ru.nstu.nest.ui.view.panel;

import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

@Component
public class ProjectSidebarPanel extends JPanel {

    private static final int DEFAULT_SPLIT = 420;

    private final KnowledgebaseSourcePanel knowledgebaseSourcePanel;
    private final ModelIntegrationPanel modelIntegrationPanel;

    public ProjectSidebarPanel(
            KnowledgebaseSourcePanel knowledgebaseSourcePanel,
            ModelIntegrationPanel modelIntegrationPanel
    ) {
        this.knowledgebaseSourcePanel = knowledgebaseSourcePanel;
        this.modelIntegrationPanel = modelIntegrationPanel;

        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 0));

        knowledgebaseSourcePanel.setBorder(BorderFactory.createTitledBorder("Базы знаний"));
        modelIntegrationPanel.setBorder(BorderFactory.createTitledBorder("Модели"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, knowledgebaseSourcePanel, modelIntegrationPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(DEFAULT_SPLIT);

        add(splitPane, BorderLayout.CENTER);
    }

}
