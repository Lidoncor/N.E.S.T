package ru.nstu.nest.files.properties.kb;

public class KnowledgebaseSourceProperties {

    private KnowledgebaseAgentType agentType = KnowledgebaseAgentType.FORWARD_CHAIN;

    public KnowledgebaseAgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(KnowledgebaseAgentType agentType) {
        this.agentType = agentType;
    }

}
