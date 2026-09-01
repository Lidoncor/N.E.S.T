package ru.nstu.nest.ui.view.dialog.common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public abstract class FormGridPanel extends JPanel {

    private int row;

    protected FormGridPanel() {
        setLayout(new GridBagLayout());
    }

    protected void addField(String label, Component component) {
        addRow(label, component, false);
    }

    protected void addBlock(String label, Component component) {
        addRow(label, component, true);
    }

    protected void addArea(String label, JTextArea area) {
        addRow(label, new JScrollPane(area), true);
    }

    protected void addBrowseField(String label, JTextField field, String buttonText, Runnable browseAction) {
        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0));
        javax.swing.JButton browseButton = new javax.swing.JButton(buttonText);
        browseButton.addActionListener(event -> browseAction.run());
        fieldPanel.add(field, BorderLayout.CENTER);
        fieldPanel.add(browseButton, BorderLayout.EAST);
        addRow(label, fieldPanel, false);
    }

    private void addRow(String label, Component component, boolean stretchVertically) {
        GridBagConstraints labelConstraints = baseConstraints();
        if (stretchVertically) {
            labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        }
        add(new JLabel(label), labelConstraints);

        GridBagConstraints valueConstraints = baseConstraints();
        valueConstraints.gridx = 1;
        valueConstraints.weightx = 1.0;
        valueConstraints.fill = stretchVertically ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        if (stretchVertically) {
            valueConstraints.weighty = 1.0;
        }
        add(component, valueConstraints);
        row++;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        return constraints;
    }

}
