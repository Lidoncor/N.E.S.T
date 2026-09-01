package ru.nstu.nest.ui.view;

import org.springframework.stereotype.Component;
import ru.nstu.nest.ui.view.component.NavigationBar;
import ru.nstu.nest.ui.view.panel.ProjectRunStatusPanel;
import ru.nstu.nest.ui.view.panel.WorkspacePanel;

import javax.swing.JFrame;
import java.awt.BorderLayout;

@Component
public class ApplicationFrame extends JFrame {

    private final WorkspacePanel workSpace;
    private final NavigationBar navigationBar;
    private final ProjectRunStatusPanel projectRunStatusPanel;

    public ApplicationFrame(
            WorkspacePanel workSpace,
            NavigationBar navigationBar,
            ProjectRunStatusPanel projectRunStatusPanel
    ) {
        this.workSpace = workSpace;
        this.navigationBar = navigationBar;
        this.projectRunStatusPanel = projectRunStatusPanel;

        initUi();
    }

    private void initUi() {
        setTitle("NEST");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(workSpace, BorderLayout.CENTER);
        add(navigationBar, BorderLayout.NORTH);
        add(projectRunStatusPanel, BorderLayout.SOUTH);
    }

}
