package ru.nstu.nest.ui.view.dialog.common;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

public abstract class FormSectionPanel extends FormGridPanel {

    protected FormSectionPanel(String title) {
        setBorder(BorderFactory.createTitledBorder(title));
    }

    protected JTextArea createArea() {
        JTextArea area = new JTextArea(4, 24);
        area.setLineWrap(false);
        return area;
    }

}
