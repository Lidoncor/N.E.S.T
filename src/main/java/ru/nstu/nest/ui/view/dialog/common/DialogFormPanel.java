package ru.nstu.nest.ui.view.dialog.common;

import javax.swing.JPanel;

public interface DialogFormPanel<T> {

    JPanel panel();

    T toFormData();

}
