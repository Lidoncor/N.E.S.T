package ru.nstu.nest.ui.presenter.actions;

import ru.nstu.nest.files.dto.KnowledgebaseSourceDescriptor;

public interface KnowledgebaseSourcePanelActions {

    void create();

    void importExisting();

    void open(KnowledgebaseSourceDescriptor descriptor);

    void edit(KnowledgebaseSourceDescriptor descriptor);

    void rename(KnowledgebaseSourceDescriptor descriptor);

    void delete(KnowledgebaseSourceDescriptor descriptor);

}
