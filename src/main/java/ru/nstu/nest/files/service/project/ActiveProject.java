package ru.nstu.nest.files.service.project;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ActiveProject {

    private ProjectStructure projectStructure;

    public ProjectStructure openProject(File root) {
        projectStructure = ProjectStructure.from(root);

        return projectStructure;
    }

    public boolean isOpen() {
        return projectStructure != null;
    }

    public ProjectStructure requireProject() {
        if (projectStructure == null) {
            throw new IllegalStateException("Проект не открыт");
        }

        return projectStructure;
    }

    public void closeProject() {
        projectStructure = null;
    }

}

