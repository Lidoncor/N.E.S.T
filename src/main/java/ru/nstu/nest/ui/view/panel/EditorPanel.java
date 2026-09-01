package ru.nstu.nest.ui.view.panel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.springframework.stereotype.Component;
import ru.nstu.nest.ui.presenter.actions.EditorPanelActions;
import ru.nstu.nest.ui.view.component.EditorTabHeader;
import ru.nstu.nest.ui.view.common.SwingDialogs;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EditorPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(248, 250, 252);
    private static final Color TEXT_BACKGROUND = Color.WHITE;

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final Map<File, EditorTab> tabs = new LinkedHashMap<>();
    private EditorPanelActions actions;

    public EditorPanel() {
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(PANEL_BACKGROUND);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.setOpaque(true);
        tabbedPane.setBackground(PANEL_BACKGROUND);
        tabbedPane.addChangeListener(event -> notifySelection());

        registerSaveShortcut();
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void bind(EditorPanelActions actions) {
        this.actions = actions;
    }

    public void showTab(File file, String content) {
        showTab(file, file.getName(), content);
    }

    public void showTab(File file, String title, String content) {
        if (tabs.containsKey(file)) {
            selectTab(file);
            return;
        }

        RSyntaxTextArea editor = createEditor();
        editor.setText(content);

        EditorTab tab = new EditorTab(editor);
        tabs.put(file, tab);

        tabbedPane.addTab(title, tab.scrollPane);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, new EditorTabHeader(file, title, actions));
        tabbedPane.setSelectedIndex(index);

        notifySelection();
    }

    public void selectTab(File file) {
        int index = new ArrayList<>(tabs.keySet()).indexOf(file);
        if (index >= 0) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public void removeTab(File file) {
        EditorTab tab = tabs.remove(file);
        if (tab == null) {
            return;
        }

        tabbedPane.remove(tab.scrollPane);
        notifySelection();
    }

    public String getContent(File file) {
        EditorTab tab = tabs.get(file);
        if (tab == null) {
            throw new IllegalStateException("Вкладка редактора не открыта для файла: " + file.getName());
        }
        return tab.textArea.getText();
    }

    public File getSelectedFile() {
        int index = tabbedPane.getSelectedIndex();
        if (index < 0 || index >= tabs.size()) {
            return null;
        }
        return new ArrayList<>(tabs.keySet()).get(index);
    }

    public void showError(String message) {
        SwingDialogs.showError(this, message, "Ошибка");
    }

    private void notifySelection() {
        File selectedFile = getSelectedFile();
        if (selectedFile != null && actions != null) {
            actions.selectionChanged(selectedFile);
        }
    }

    private void registerSaveShortcut() {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                "saveFile"
        );
        getActionMap().put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (actions != null) {
                    actions.saveRequested();
                }
            }
        });
    }

    private RSyntaxTextArea createEditor() {
        RSyntaxTextArea area = new RSyntaxTextArea();
        area.setHighlightCurrentLine(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        area.setCodeFoldingEnabled(true);
        area.setBackground(TEXT_BACKGROUND);
        return area;
    }

    private static class EditorTab {
        private final RSyntaxTextArea textArea;
        private final RTextScrollPane scrollPane;

        private EditorTab(RSyntaxTextArea textArea) {
            this.textArea = textArea;
            this.scrollPane = new RTextScrollPane(textArea);

            scrollPane.setLineNumbersEnabled(true);
            scrollPane.setFoldIndicatorEnabled(true);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            scrollPane.setBackground(PANEL_BACKGROUND);
            scrollPane.getViewport().setBackground(TEXT_BACKGROUND);
            scrollPane.getGutter().setBackground(PANEL_BACKGROUND);
        }
    }

}
