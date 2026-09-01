package ru.nstu.nest.ui.view.dialog.model;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.FormSectionPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ModelOutputSection extends FormSectionPanel {

    private static final Color SELECTED_BORDER = new Color(59, 130, 246);

    private final JTextField outputTensorNameField = new JTextField(28);
    private final JLabel hintLabel = new JLabel(" ");
    private final JTextField resultFactField = new JTextField(28);
    private final JPanel rowsPanel = new JPanel();
    private final List<ClassMappingRow> rows = new ArrayList<>();
    private final JButton removeButton = new JButton("Удалить");
    private final JButton upButton = new JButton("Вверх");
    private final JButton downButton = new JButton("Вниз");
    private int selectedRow = -1;

    ModelOutputSection(ModelIntegrationDescriptor descriptor) {
        super("Выход");
        addField("Имя выходного тензора", outputTensorNameField);
        addField("Подсказка", hintLabel);
        addBlock("Соответствие классов", buildEditor());
        addField("Факт результата", resultFactField);

        if (descriptor != null) {
            outputTensorNameField.setText(DialogFieldValues.defaultValue(descriptor.config().getOutput().getTensorName()));
            resultFactField.setText(descriptor.config().getOutput().getResultMapping().getFact());
            descriptor.config().getOutput().getClassMapping().stream()
                    .sorted(Comparator.comparingInt(ModelSourceProperties.ClassMapping::getIndex))
                    .forEach(mapping -> rows.add(new ClassMappingRow(mapping.getLabel(), mapping.getFact())));
        }
        if (rows.isEmpty()) {
            rows.add(new ClassMappingRow("", ""));
        }
        rebuildRows();
        selectRow(0);
    }

    String outputTensorName() {
        return DialogFieldValues.optionalValue(outputTensorNameField.getText());
    }

    String currentTensorName() {
        return DialogFieldValues.optionalValue(outputTensorNameField.getText());
    }

    List<ModelSourceProperties.ClassMapping> classMapping() {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Добавьте хотя бы одно соответствие класса");
        }

        List<ModelSourceProperties.ClassMapping> classMapping = new ArrayList<>(rows.size());
        for (int index = 0; index < rows.size(); index++) {
            ClassMappingRow row = rows.get(index);
            ModelSourceProperties.ClassMapping mapping = new ModelSourceProperties.ClassMapping();
            mapping.setIndex(index);
            mapping.setLabel(DialogFieldValues.requireValue(
                    row.labelField.getText(),
                    "Укажите метку класса в строке #" + index
            ));
            mapping.setFact(DialogFieldValues.requireValue(
                    row.factField.getText(),
                    "Укажите факт класса в строке #" + index
            ));
            classMapping.add(mapping);
        }
        return List.copyOf(classMapping);
    }

    String resultFact() {
        return DialogFieldValues.requireValue(resultFactField.getText(), "Укажите факт результата");
    }

    void applyModelHint(String tensorName, String hintText, Integer expectedClassCount) {
        if ((outputTensorNameField.getText() == null || outputTensorNameField.getText().isBlank())
                && tensorName != null && !tensorName.isBlank()) {
            outputTensorNameField.setText(tensorName);
        }
        hintLabel.setText(hintText == null || hintText.isBlank() ? " " : hintText);

        if (expectedClassCount != null && expectedClassCount > 0 && rowsAreBlank()) {
            ensureRowCount(expectedClassCount);
        }
    }

    void clearModelHint() {
        hintLabel.setText(" ");
    }

    private JPanel buildEditor() {
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setOpaque(false);

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(_ -> addRow());
        removeButton.addActionListener(_ -> removeSelectedRow());
        upButton.addActionListener(_ -> moveSelectedRow(-1));
        downButton.addActionListener(_ -> moveSelectedRow(1));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        toolbar.add(addButton);
        toolbar.add(removeButton);
        toolbar.add(upButton);
        toolbar.add(downButton);

        JPanel editor = new JPanel(new BorderLayout(0, 8));
        editor.setOpaque(false);
        editor.add(toolbar, BorderLayout.NORTH);
        editor.add(rowsPanel, BorderLayout.CENTER);
        return editor;
    }

    private void addRow() {
        rows.add(new ClassMappingRow("", ""));
        rebuildRows();
        selectRow(rows.size() - 1);
        rows.get(selectedRow).labelField.requestFocusInWindow();
    }

    private void removeSelectedRow() {
        if (selectedRow < 0 || selectedRow >= rows.size()) {
            return;
        }

        rows.remove(selectedRow);
        if (rows.isEmpty()) {
            selectedRow = -1;
            rebuildRows();
            updateSelectionState();
            return;
        }

        rebuildRows();
        selectRow(Math.min(selectedRow, rows.size() - 1));
    }

    private void moveSelectedRow(int delta) {
        if (selectedRow < 0) {
            return;
        }

        int targetIndex = selectedRow + delta;
        if (targetIndex < 0 || targetIndex >= rows.size()) {
            return;
        }

        ClassMappingRow selected = rows.remove(selectedRow);
        rows.add(targetIndex, selected);
        rebuildRows();
        selectRow(targetIndex);
    }

    private void selectRow(int index) {
        if (rows.isEmpty()) {
            selectedRow = -1;
        } else {
            selectedRow = Math.max(0, Math.min(index, rows.size() - 1));
        }
        updateSelectionState();
    }

    private void rebuildRows() {
        rowsPanel.removeAll();
        for (int index = 0; index < rows.size(); index++) {
            ClassMappingRow row = rows.get(index);
            row.updateIndex(index);
            rowsPanel.add(row);
            if (index < rows.size() - 1) {
                rowsPanel.add(Box.createVerticalStrut(4));
            }
        }
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private void updateSelectionState() {
        for (int index = 0; index < rows.size(); index++) {
            rows.get(index).updateIndex(index);
            rows.get(index).setSelected(index == selectedRow);
        }
        removeButton.setEnabled(selectedRow >= 0);
        upButton.setEnabled(selectedRow > 0);
        downButton.setEnabled(selectedRow >= 0 && selectedRow < rows.size() - 1);
        rowsPanel.repaint();
    }

    private boolean rowsAreBlank() {
        return rows.stream().allMatch(row ->
                row.labelField.getText().isBlank() && row.factField.getText().isBlank()
        );
    }

    private void ensureRowCount(int count) {
        while (rows.size() < count) {
            rows.add(new ClassMappingRow("", ""));
        }
        while (rows.size() > count) {
            rows.removeLast();
        }
        rebuildRows();
        selectRow(rows.isEmpty() ? -1 : Math.min(selectedRow >= 0 ? selectedRow : 0, rows.size() - 1));
    }

    private final class ClassMappingRow extends JPanel {

        private final JLabel indexLabel = new JLabel();
        private final JTextField labelField = new JTextField(12);
        private final JTextField factField = new JTextField(18);

        private ClassMappingRow(String label, String fact) {
            setLayout(new GridBagLayout());
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setOpaque(true);

            indexLabel.setText("#0");
            indexLabel.setPreferredSize(new Dimension(34, labelField.getPreferredSize().height));
            labelField.setText(label);
            factField.setText(fact);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(0, 0, 0, 8);
            constraints.gridy = 0;
            constraints.gridx = 0;
            add(indexLabel, constraints);

            constraints.gridx = 1;
            add(new JLabel("Метка"), constraints);

            constraints.gridx = 2;
            constraints.weightx = 0.35;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(labelField, constraints);

            constraints.gridx = 3;
            constraints.weightx = 0;
            add(new JLabel("Факт"), constraints);

            constraints.gridx = 4;
            constraints.weightx = 0.65;
            add(factField, constraints);

            bindSelection(this);
            bindSelection(labelField);
            bindSelection(factField);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        private void updateIndex(int index) {
            indexLabel.setText("#" + index);
        }

        private void setSelected(boolean selected) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? SELECTED_BORDER : Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        }

        private void bindSelection(Component component) {
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    selectRow(rows.indexOf(ClassMappingRow.this));
                }
            });
            if (component instanceof JTextField field) {
                field.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent event) {
                        selectRow(rows.indexOf(ClassMappingRow.this));
                    }
                });
            }
        }
    }
}
