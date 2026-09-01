package ru.nstu.nest.files.service.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ActiveProject activeProject;
    private final ProjectSettingsService projectSettingsService;

    public ProjectStructure createProject(File root) {
        ProjectStructure projectStructure = ProjectStructure.from(root);
        createProjectStructure(root);
        projectSettingsService.saveDefault(projectStructure);
        return activeProject.openProject(root);
    }

    public ProjectStructure openProject(File root) {
        validateProject(root);
        return activeProject.openProject(root);
    }

    public String readFile(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл: " + file.getName(), e);
        }
    }

    public void writeFile(File file, String content) {
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить файл: " + file.getName(), e);
        }
    }

    private void createProjectStructure(File root) {
        new File(root, "knowledgebase").mkdirs();
        new File(root, "model").mkdirs();
        new File(root, "runtime").mkdirs();
        new File(root, "runtime/attachments").mkdirs();
    }

    private void validateProject(File root) {
        File knowledgebaseDir = new File(root, "knowledgebase");
        File modelDir = new File(root, "model");
        if (!knowledgebaseDir.exists() || !modelDir.exists()) {
            throw new IllegalStateException("Некорректная структура проекта: отсутствует каталог баз знаний или моделей");
        }
    }

}

