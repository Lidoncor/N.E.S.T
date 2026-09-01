package ru.nstu.nest.ui.presenter.actions;

import ru.nstu.nest.files.dto.ModelIntegrationDescriptor;

public interface ModelIntegrationPanelActions {

    void importExisting();

    void edit(ModelIntegrationDescriptor descriptor);

    void rename(ModelIntegrationDescriptor descriptor);

    void delete(ModelIntegrationDescriptor descriptor);

}
