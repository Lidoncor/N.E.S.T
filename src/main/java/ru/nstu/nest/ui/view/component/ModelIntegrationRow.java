package ru.nstu.nest.ui.view.component;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.ui.presenter.actions.ModelIntegrationPanelActions;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModelIntegrationRow extends RoundedCardPanel {

    private static final Color ROW_BACKGROUND = new Color(223, 228, 233);
    private static final Color ROW_HOVER_BACKGROUND = new Color(233, 237, 241);
    private static final int ROW_ARC = 16;

    public ModelIntegrationRow(
            ModelIntegrationDescriptor descriptor,
            ModelIntegrationPanelActions actions
    ) {
        super(ROW_ARC);

        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(ROW_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));

        JLabel nameLabel = new JLabel(descriptor.name());
        nameLabel.setToolTipText(descriptor.name());

        installHover(nameLabel);
        installContextMenu(descriptor, actions, nameLabel);

        add(nameLabel, BorderLayout.CENTER);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    private void installHover(Component... components) {
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                setBackground(ROW_HOVER_BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point point = javax.swing.SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), ModelIntegrationRow.this);
                if (contains(point)) {
                    return;
                }
                setBackground(ROW_BACKGROUND);
            }
        };

        addListeners(hoverListener, components);
        addMouseListener(hoverListener);
    }

    private void installContextMenu(
            ModelIntegrationDescriptor descriptor,
            ModelIntegrationPanelActions actions,
            Component... components
    ) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Параметры");
        editItem.addActionListener(event -> actions.edit(descriptor));
        menu.add(editItem);

        JMenuItem renameItem = new JMenuItem("Переименовать");
        renameItem.addActionListener(event -> actions.rename(descriptor));
        menu.add(renameItem);

        JMenuItem removeItem = new JMenuItem("Удалить");
        removeItem.addActionListener(event -> actions.delete(descriptor));
        menu.add(removeItem);

        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                showPopup(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showPopup(event);
            }

            private void showPopup(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    menu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        };

        addListeners(popupListener, components);
        addMouseListener(popupListener);
    }

    private void addListeners(MouseAdapter listener, Component... components) {
        for (Component component : components) {
            component.addMouseListener(listener);
        }
    }

}
