package ru.nstu.nest.ui.view.dialog.model;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;
import ru.nstu.nest.files.properties.model.ImageColorMode;
import ru.nstu.nest.files.properties.model.ImageInputTensorSpec;
import ru.nstu.nest.files.properties.model.ImageTensorLayout;
import ru.nstu.nest.files.properties.model.ModelInputType;
import ru.nstu.nest.files.properties.model.ModelSourceProperties;
import ru.nstu.nest.ui.view.dialog.common.DialogFieldValues;
import ru.nstu.nest.ui.view.dialog.common.FormSectionPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ModelInputSection extends FormSectionPanel {

    private static final Color SELECTED_BORDER = new Color(59, 130, 246);
    private static final String FACT_VECTOR_CARD = "fact-vector";
    private static final String IMAGE_FACT_CARD = "image-fact";

    private final JComboBox<InputTypeOption> inputTypeComboBox = new JComboBox<>(InputTypeOption.options());
    private final JTextField inputTensorNameField = new JTextField(28);
    private final JLabel hintLabel = new JLabel(" ");
    private final CardLayout inputCardLayout = new CardLayout();
    private final JPanel inputCards = new JPanel(inputCardLayout);
    private final JPanel rowsPanel = new JPanel();
    private final JTextField imageFactField = new JTextField(24);
    private final JTextField imageWidthField = new JTextField(8);
    private final JTextField imageHeightField = new JTextField(8);
    private final JComboBox<ImageColorMode> colorModeComboBox = new JComboBox<>(ImageColorMode.values());
    private final JComboBox<ImageTensorLayout> layoutComboBox = new JComboBox<>(ImageTensorLayout.values());
    private final JCheckBox normalizeCheckBox = new JCheckBox("", true);
    private final JToggleButton imageAdvancedToggleButton = new JToggleButton();
    private final JPanel imageAdvancedPanel = new JPanel();
    private final List<FeatureRow> rows = new ArrayList<>();
    private final JButton removeButton = new JButton("Удалить");
    private final JButton upButton = new JButton("Вверх");
    private final JButton downButton = new JButton("Вниз");
    private int selectedRow = -1;

    ModelInputSection(ModelIntegrationDescriptor descriptor) {
        super("Вход");
        addField("Тип входных данных", inputTypeComboBox);
        addField("Имя входного тензора", inputTensorNameField);
        addField("Подсказка", hintLabel);
        addBlock("Параметры входа", buildInputCards());

        if (descriptor != null) {
            ModelSourceProperties.Input input = descriptor.config().getInput();
            inputTypeComboBox.setSelectedItem(InputTypeOption.of(resolveInputType(input)));
            inputTensorNameField.setText(input.getTensorName());
            input.getFeatures().stream()
                    .sorted(Comparator.comparingInt(ModelSourceProperties.Feature::getIndex))
                    .forEach(feature -> rows.add(new FeatureRow(feature.getFact())));
            applyImageConfig(input.getImage());
        }
        if (rows.isEmpty()) {
            rows.add(new FeatureRow(""));
        }
        inputTypeComboBox.addActionListener(_ -> updateInputCard());
        rebuildRows();
        selectRow(0);
        updateInputCard();
    }

    String inputTensorName() {
        return DialogFieldValues.requireValue(inputTensorNameField.getText(), "Укажите имя входного тензора");
    }

    String currentTensorName() {
        return DialogFieldValues.optionalValue(inputTensorNameField.getText());
    }

    ModelInputType inputType() {
        return selectedInputType();
    }

    List<ModelSourceProperties.Feature> features() {
        if (selectedInputType() == ModelInputType.IMAGE_FACT) {
            return List.of();
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Добавьте хотя бы один входной признак");
        }

        List<ModelSourceProperties.Feature> features = new ArrayList<>(rows.size());
        for (int index = 0; index < rows.size(); index++) {
            ModelSourceProperties.Feature feature = new ModelSourceProperties.Feature();
            feature.setIndex(index);
            feature.setFact(DialogFieldValues.requireValue(
                    rows.get(index).factField.getText(),
                    "Укажите факт входного признака в строке #" + index
            ));
            features.add(feature);
        }
        return List.copyOf(features);
    }

    ModelSourceProperties.Image imageInput() {
        ModelSourceProperties.Image image = new ModelSourceProperties.Image();
        if (selectedInputType() == ModelInputType.FACT_VECTOR) {
            return image;
        }

        image.setFact(DialogFieldValues.requireValue(imageFactField.getText(), "Укажите факт изображения"));
        image.setWidth(parsePositiveInt(imageWidthField.getText(), "Укажите ширину изображения"));
        image.setHeight(parsePositiveInt(imageHeightField.getText(), "Укажите высоту изображения"));
        image.setColorMode((ImageColorMode) colorModeComboBox.getSelectedItem());
        image.setLayout((ImageTensorLayout) layoutComboBox.getSelectedItem());
        image.setNormalize(normalizeCheckBox.isSelected());
        return image;
    }

    void applyModelHint(
            String tensorName,
            String hintText,
            Integer expectedFeatureCount,
            ImageInputTensorSpec imageSpec
    ) {
        if ((inputTensorNameField.getText() == null || inputTensorNameField.getText().isBlank())
                && tensorName != null && !tensorName.isBlank()) {
            inputTensorNameField.setText(tensorName);
        }
        hintLabel.setText(hintText == null || hintText.isBlank() ? " " : hintText);

        if (expectedFeatureCount != null && expectedFeatureCount > 0 && rowsAreBlank()) {
            ensureRowCount(expectedFeatureCount);
        }
        applyImageSpec(imageSpec);
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

    private JPanel buildInputCards() {
        inputCards.setOpaque(false);
        inputCards.add(buildEditor(), FACT_VECTOR_CARD);
        inputCards.add(buildImageEditor(), IMAGE_FACT_CARD);
        return inputCards;
    }

    private JPanel buildImageEditor() {
        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
        editor.setOpaque(false);
        editor.add(fieldRow("Факт изображения", imageFactField));
        editor.add(Box.createVerticalStrut(10));
        editor.add(buildImageAdvancedToggle());
        editor.add(Box.createVerticalStrut(6));
        editor.add(buildImageAdvancedPanel());
        updateImageAdvancedPanel();
        return editor;
    }

    private JPanel buildImageAdvancedToggle() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        imageAdvancedToggleButton.setFocusable(false);
        imageAdvancedToggleButton.addActionListener(_ -> updateImageAdvancedPanel());
        panel.add(imageAdvancedToggleButton);

        return panel;
    }

    private JPanel buildImageAdvancedPanel() {
        imageAdvancedPanel.setLayout(new BoxLayout(imageAdvancedPanel, BoxLayout.Y_AXIS));
        imageAdvancedPanel.setOpaque(false);
        imageAdvancedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        imageAdvancedPanel.add(fieldRow("Ширина", imageWidthField));
        imageAdvancedPanel.add(Box.createVerticalStrut(6));
        imageAdvancedPanel.add(fieldRow("Высота", imageHeightField));
        imageAdvancedPanel.add(Box.createVerticalStrut(6));
        imageAdvancedPanel.add(fieldRow("Цветовой режим", colorModeComboBox));
        imageAdvancedPanel.add(Box.createVerticalStrut(6));
        imageAdvancedPanel.add(fieldRow("Layout", layoutComboBox));
        imageAdvancedPanel.add(Box.createVerticalStrut(6));
        normalizeCheckBox.setOpaque(false);
        imageAdvancedPanel.add(fieldRow("Нормализация", normalizeCheckBox));

        return imageAdvancedPanel;
    }

    private void updateImageAdvancedPanel() {
        boolean expanded = imageAdvancedToggleButton.isSelected();
        imageAdvancedToggleButton.setText(
                expanded
                        ? "Скрыть дополнительные параметры изображения"
                        : "Показать дополнительные параметры изображения"
        );
        imageAdvancedPanel.setVisible(expanded);
        imageAdvancedPanel.revalidate();
        imageAdvancedPanel.repaint();
    }

    private JPanel fieldRow(String label, Component component) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel rowLabel = new JLabel(label);
        rowLabel.setPreferredSize(new Dimension(130, component.getPreferredSize().height));
        row.add(rowLabel, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private void applyImageConfig(ModelSourceProperties.Image image) {
        if (image == null) {
            return;
        }
        imageFactField.setText(image.getFact());
        if (image.getWidth() > 0) {
            imageWidthField.setText(Integer.toString(image.getWidth()));
        }
        if (image.getHeight() > 0) {
            imageHeightField.setText(Integer.toString(image.getHeight()));
        }
        colorModeComboBox.setSelectedItem(image.getColorMode());
        layoutComboBox.setSelectedItem(image.getLayout());
        normalizeCheckBox.setSelected(image.isNormalize());
    }

    private void applyImageSpec(ImageInputTensorSpec imageSpec) {
        if (imageSpec == null || !imageSizeFieldsAreBlank()) {
            return;
        }

        imageWidthField.setText(Integer.toString(imageSpec.width()));
        imageHeightField.setText(Integer.toString(imageSpec.height()));
        colorModeComboBox.setSelectedItem(imageSpec.colorMode());
        layoutComboBox.setSelectedItem(imageSpec.layout());
    }

    private boolean imageSizeFieldsAreBlank() {
        return imageWidthField.getText().isBlank() && imageHeightField.getText().isBlank();
    }

    private void updateInputCard() {
        inputCardLayout.show(inputCards, selectedInputType() == ModelInputType.IMAGE_FACT
                ? IMAGE_FACT_CARD
                : FACT_VECTOR_CARD);
        inputCards.revalidate();
        inputCards.repaint();
    }

    private ModelInputType selectedInputType() {
        Object selected = inputTypeComboBox.getSelectedItem();
        if (selected instanceof InputTypeOption option) {
            return option.type();
        }
        return ModelInputType.FACT_VECTOR;
    }

    private ModelInputType resolveInputType(ModelSourceProperties.Input input) {
        return input.getType() == null ? ModelInputType.FACT_VECTOR : input.getType();
    }

    private int parsePositiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(DialogFieldValues.requireValue(value, message));
            if (parsed <= 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private void addRow() {
        rows.add(new FeatureRow(""));
        rebuildRows();
        selectRow(rows.size() - 1);
        rows.get(selectedRow).factField.requestFocusInWindow();
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

        FeatureRow selected = rows.remove(selectedRow);
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
            FeatureRow row = rows.get(index);
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
        return rows.stream().allMatch(row -> row.factField.getText().isBlank());
    }

    private void ensureRowCount(int count) {
        while (rows.size() < count) {
            rows.add(new FeatureRow(""));
        }
        while (rows.size() > count) {
            rows.removeLast();
        }
        rebuildRows();
        selectRow(rows.isEmpty() ? -1 : Math.min(selectedRow >= 0 ? selectedRow : 0, rows.size() - 1));
    }

    private final class FeatureRow extends JPanel {

        private final JLabel indexLabel = new JLabel();
        private final JTextField factField = new JTextField(24);

        private FeatureRow(String fact) {
            setLayout(new BorderLayout(8, 0));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setOpaque(true);

            indexLabel.setPreferredSize(new Dimension(34, factField.getPreferredSize().height));
            factField.setText(fact);

            add(indexLabel, BorderLayout.WEST);
            add(factField, BorderLayout.CENTER);

            bindSelection(this);
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
                    selectRow(rows.indexOf(FeatureRow.this));
                }
            });
            if (component instanceof JTextField field) {
                field.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent event) {
                        selectRow(rows.indexOf(FeatureRow.this));
                    }
                });
            }
        }
    }

    private record InputTypeOption(ModelInputType type, String label) {

        private static InputTypeOption[] options() {
            return new InputTypeOption[]{
                    new InputTypeOption(ModelInputType.FACT_VECTOR, "Вектор признаков"),
                    new InputTypeOption(ModelInputType.IMAGE_FACT, "Изображение из факта")
            };
        }

        private static InputTypeOption of(ModelInputType type) {
            for (InputTypeOption option : options()) {
                if (option.type() == type) {
                    return option;
                }
            }
            return options()[0];
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
