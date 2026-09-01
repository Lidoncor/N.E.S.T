package ru.nstu.nest.ui.view.component;

import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;
import ru.nstu.nest.ui.presenter.actions.KnowledgebaseSourcePanelActions;
import ru.nstu.nest.ui.model.KnowledgebaseSourceInputStatus;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KnowledgebaseSourceRow extends RoundedCardPanel {

    private static final Color ROW_BACKGROUND = new Color(223, 228, 233);
    private static final Color ROW_HOVER_BACKGROUND = new Color(233, 237, 241);
    private static final Color INCOMPLETE_BORDER = new Color(232, 154, 154);
    private static final Color COMPLETE_BORDER = new Color(124, 199, 154);
    private static final int ROW_ARC = 16;
    private static final int STATUS_STROKE_WIDTH = 2;

    private KnowledgebaseSourceInputStatus inputStatus = KnowledgebaseSourceInputStatus.NONE;

    public KnowledgebaseSourceRow(
            KnowledgebaseSourceDescriptor descriptor,
            KnowledgebaseSourcePanelActions actions
    ) {
        super(ROW_ARC);

        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(ROW_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(descriptor.name());
        nameLabel.setToolTipText(descriptor.name());

        installHover(nameLabel);
        installOpen(descriptor, actions, nameLabel);
        installContextMenu(descriptor, actions, nameLabel);

        add(nameLabel, BorderLayout.CENTER);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    public void setInputStatus(KnowledgebaseSourceInputStatus inputStatus) {
        this.inputStatus = inputStatus == null ? KnowledgebaseSourceInputStatus.NONE : inputStatus;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Color borderColor = switch (inputStatus) {
            case INCOMPLETE -> INCOMPLETE_BORDER;
            case COMPLETE -> COMPLETE_BORDER;
            case NONE -> null;
        };
        if (borderColor == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(STATUS_STROKE_WIDTH));
            int offset = STATUS_STROKE_WIDTH / 2;
            g2.drawRoundRect(
                    offset,
                    offset,
                    getWidth() - STATUS_STROKE_WIDTH,
                    getHeight() - STATUS_STROKE_WIDTH,
                    ROW_ARC,
                    ROW_ARC
            );
        } finally {
            g2.dispose();
        }
    }

    private void installHover(Component... components) {
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                setBackground(ROW_HOVER_BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point point = javax.swing.SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), KnowledgebaseSourceRow.this);
                if (contains(point)) {
                    return;
                }
                setBackground(ROW_BACKGROUND);
            }
        };

        addListeners(hoverListener, components);
        addMouseListener(hoverListener);
    }

    private void installOpen(
            KnowledgebaseSourceDescriptor descriptor,
            KnowledgebaseSourcePanelActions actions,
            Component... components
    ) {
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
                    actions.open(descriptor);
                }
            }
        };

        addListeners(clickListener, components);
        addMouseListener(clickListener);
    }

    private void installContextMenu(
            KnowledgebaseSourceDescriptor descriptor,
            KnowledgebaseSourcePanelActions actions,
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
