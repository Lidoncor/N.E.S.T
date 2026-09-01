package ru.nstu.nest.ui.service;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.source.kb.metadata.FrameDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FrameInputValidator {

    public static final String REQUIRED_FIELDS_ERROR = "Заполните все обязательные поля";
    public static final String NUMERIC_FIELD_ERROR = "Поле должно содержать число";

    public List<String> validate(FrameDefinition frame, Map<String, String> valuesBySlotName) {
        List<String> errors = new ArrayList<>();

        boolean hasBlankFields = frame.slots().stream()
                .anyMatch(slot -> isBlank(valuesBySlotName.get(slot.name())));
        if (hasBlankFields) {
            errors.add(REQUIRED_FIELDS_ERROR);
        }

        boolean hasInvalidNumericFields = frame.slots().stream()
                .filter(slot -> slot.numeric() && !isBlank(valuesBySlotName.get(slot.name())))
                .anyMatch(slot -> !isNumber(valuesBySlotName.get(slot.name())));
        if (hasInvalidNumericFields) {
            errors.add(NUMERIC_FIELD_ERROR);
        }

        return errors;
    }

    public boolean isNumber(String value) {
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
