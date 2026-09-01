package ru.nstu.nest.files.service.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nstu.nest.files.properties.loader.ProjectSettingsPropertiesLoader;
import ru.nstu.nest.files.properties.project.ProjectSettingsProperties;

@Service
@RequiredArgsConstructor
public class ProjectSettingsService {

    private final ProjectSettingsPropertiesLoader loader;

    public ProjectSettingsProperties load(ProjectStructure projectStructure) {
        return loader.load(projectStructure.settingsPath());
    }

    public void save(ProjectStructure projectStructure, ProjectSettingsProperties settings) {
        loader.save(projectStructure.settingsPath(), settings);
    }

    public void saveDefault(ProjectStructure projectStructure) {
        save(projectStructure, new ProjectSettingsProperties());
    }

}
