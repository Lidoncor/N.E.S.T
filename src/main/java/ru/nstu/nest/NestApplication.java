package ru.nstu.nest;

import com.formdev.flatlaf.FlatLightLaf;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.nstu.nest.ui.view.ApplicationFrame;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Locale;

@SpringBootApplication
public class NestApplication {

    static void main(String[] args) {
        Locale.setDefault(Locale.of("ru", "RU"));
        FlatLightLaf.setup();
        configureUiDefaults();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(NestApplication.class)
                .headless(false)
                .run(args);

        EventQueue.invokeLater(() -> {
            ApplicationFrame applicationFrame = context.getBean(ApplicationFrame.class);
            applicationFrame.setVisible(true);
        });
    }

    private static void configureUiDefaults() {
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.innerOutlineWidth", 0);
        UIManager.put("ScrollPane.focusWidth", 0);
        UIManager.put("Component.focusColor", new Color(0, 0, 0, 0));
        UIManager.put("OptionPane.okButtonText", "ОК");
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.saveButtonText", "Сохранить");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.lookInLabelText", "Папка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов");
        UIManager.put("FileChooser.upFolderToolTipText", "На уровень выше");
        UIManager.put("FileChooser.homeFolderToolTipText", "Домашняя папка");
        UIManager.put("FileChooser.newFolderToolTipText", "Создать папку");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Подробности");
        copyUiColor("Button.focusedBackground", "Button.background");
        copyUiColor("Button.focusedBorderColor", "Component.borderColor");
        copyUiColor("Button.default.focusedBackground", "Button.default.background");
        copyUiColor("Button.default.focusedBorderColor", "Button.default.borderColor");
        copyUiColor("ScrollPane.focusedBorderColor", "ScrollPane.borderColor");
        copyUiColor("ToggleButton.focusedBackground", "ToggleButton.background");
        copyUiColor("ToggleButton.focusedBorderColor", "Component.borderColor");
    }

    private static void copyUiColor(String targetKey, String sourceKey) {
        Object value = UIManager.get(sourceKey);
        if (value != null) {
            UIManager.put(targetKey, value);
        }
    }

}
