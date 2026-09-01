package ru.nstu.nest.files.properties.project;

public class ProjectSettingsProperties {

    public static final int DEFAULT_INFERENCE_CYCLE_LIMIT = 100;

    private int inferenceCycleLimit = DEFAULT_INFERENCE_CYCLE_LIMIT;

    public int getInferenceCycleLimit() {
        return inferenceCycleLimit;
    }

    public void setInferenceCycleLimit(int inferenceCycleLimit) {
        this.inferenceCycleLimit = inferenceCycleLimit;
    }

}
