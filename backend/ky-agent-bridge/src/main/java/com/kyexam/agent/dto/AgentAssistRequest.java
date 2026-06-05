package com.kyexam.agent.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AgentAssistRequest {
    @NotNull
    private Long nodeId;

    @NotBlank
    private String feeling;

    private String plainUnderstanding;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public String getPlainUnderstanding() {
        return plainUnderstanding;
    }

    public void setPlainUnderstanding(String plainUnderstanding) {
        this.plainUnderstanding = plainUnderstanding;
    }
}
