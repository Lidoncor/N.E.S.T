package ru.nstu.nest.ui.presenter.actions;

import java.io.File;

public interface EditorPanelActions {

    void closeRequested(File file);

    void selectionChanged(File file);

    void saveRequested();

}
