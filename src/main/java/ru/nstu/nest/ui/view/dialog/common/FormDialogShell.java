package ru.nstu.nest.ui.view.dialog.common;

import ru.nstu.nest.ui.view.common.SwingDialogs;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;

public final class FormDialogShell<T> extends JDialog {

    private T result;

    public FormDialogShell(Window owner, String title, DialogFormPanel<T> formPanel) {
        this(owner, title, formPanel, null);
    }

    public FormDialogShell(Window owner, String title, DialogFormPanel<T> formPanel, Dimension preferredSize) {
        this(owner, title, formPanel, preferredSize, null, false);
    }

    public FormDialogShell(
            Window owner,
            String title,
            DialogFormPanel<T> formPanel,
            Dimension preferredSize,
            Dimension minimumSize,
            boolean resizable
    ) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        initUi(formPanel, preferredSize, minimumSize, resizable);
    }

    public T showDialog() {
        setVisible(true);
        return result;
    }

    private void initUi(
            DialogFormPanel<T> formPanel,
            Dimension preferredSize,
            Dimension minimumSize,
            boolean resizable
    ) {
        setLayout(new BorderLayout(0, 8));

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(formPanel.panel(), BorderLayout.CENTER);

        JButton okButton = new JButton("Применить");
        okButton.addActionListener(event -> submit(formPanel));

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(event -> dispose());

        JPanel buttons = new JPanel();
        buttons.add(okButton);
        buttons.add(cancelButton);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (preferredSize != null) {
            setPreferredSize(preferredSize);
        }

        pack();
        if (preferredSize != null) {
            setSize(
                    Math.max(getWidth(), preferredSize.width),
                    Math.max(getHeight(), preferredSize.height)
            );
        }
        setMinimumSize(minimumSize != null ? minimumSize : getSize());
        setResizable(resizable);
        setLocationRelativeTo(getOwner());
        getRootPane().setDefaultButton(okButton);
    }

    private void submit(DialogFormPanel<T> formPanel) {
        try {
            result = formPanel.toFormData();
            dispose();
        } catch (IllegalArgumentException ex) {
            SwingDialogs.showError(this, ex.getMessage(), "Ошибка проверки");
        }
    }

}
