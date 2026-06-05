package com.kyexam.learning.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LearningNodeConnectionRequest {
    @NotNull
    private Long sourceNodeId;

    @NotNull
    private Long targetNodeId;

    @Size(max = 32)
    private String relationType;

    @Size(max = 160)
    private String label;

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
