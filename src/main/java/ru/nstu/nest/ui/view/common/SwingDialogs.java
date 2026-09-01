package ru.nstu.nest.ui.view.common;

import lombok.experimental.UtilityClass;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;

@UtilityClass
public final class SwingDialogs {

    public static void showError(Component anchor, String message, String title) {
        JOptionPane.showMessageDialog(resolveOwner(anchor), message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static int showConfirm(Component anchor, String message, String title, int optionType) {
        return JOptionPane.showConfirmDialog(resolveOwner(anchor), message, title, optionType);
    }

    public static int showOpenDialog(JFileChooser chooser, Component anchor) {
        return chooser.showOpenDialog(resolveOwner(anchor));
    }

    public static int showSaveDialog(JFileChooser chooser, Component anchor) {
        return chooser.showSaveDialog(resolveOwner(anchor));
    }

    public static String showInput(Component anchor, String message, String title, String initialValue) {
        Object value = JOptionPane.showInputDialog(
                resolveOwner(anchor),
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                initialValue
        );
        return value instanceof String text ? text : null;
    }

    public static Window resolveOwner(Component anchor) {
        if (anchor instanceof Window window) {
            return window;
        }

        Window ancestor = anchor != null ? SwingUtilities.getWindowAncestor(anchor) : null;
        if (ancestor != null) {
            return ancestor;
        }

        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }

}
