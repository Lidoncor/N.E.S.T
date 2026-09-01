package ru.nstu.nest.ui.view.component;

import org.springframework.stereotype.Component;
import ru.nstu.nest.ui.view.common.SwingDialogs;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import java.io.File;

@Component
public class NavigationBar extends JToolBar {

    private final JMenuItem createProjectItem = new JMenuItem("Создать проект");
    private final JMenuItem openProjectItem = new JMenuItem("Открыть проект");
    private final JMenuItem saveItem = new JMenuItem("Сохранить");

    public NavigationBar() {
        initUi();
    }

    private void initUi() {
        setFloatable(false);
        setSaveEnabled(false);

        JPopupMenu fileMenu = new JPopupMenu();
        fileMenu.add(createProjectItem);
        fileMenu.add(openProjectItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);

        JButton fileButton = new JButton("Файл");
        fileButton.addActionListener(event ->
                fileMenu.show(fileButton, 0, fileButton.getHeight())
        );

        add(fileButton);
    }

    public void onCreateProject(Runnable action) {
        createProjectItem.addActionListener(_ -> action.run());
    }

    public void onOpenProject(Runnable action) {
        openProjectItem.addActionListener(_ -> action.run());
    }

    public void onSave(Runnable action) {
        saveItem.addActionListener(_ -> action.run());
    }

    public void setSaveEnabled(boolean enabled) {
        saveItem.setEnabled(enabled);
    }

    public File chooseDirectoryToCreateProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Создание проекта");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Создать");
        chooser.setSelectedFile(new File(""));

        int result = SwingDialogs.showSaveDialog(chooser, this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    public File chooseDirectoryToOpenProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открытие проекта");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Открыть");

        int result = SwingDialogs.showOpenDialog(chooser, this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    public void showError(String message) {
        SwingDialogs.showError(this, message, "Ошибка");
    }

}
