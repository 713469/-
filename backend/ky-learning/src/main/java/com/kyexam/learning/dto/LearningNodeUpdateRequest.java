package com.kyexam.learning.dto;

import com.kyexam.common.enums.LearningStatus;

import javax.validation.constraints.NotNull;

public class LearningNodeUpdateRequest {
    private String title;
    private String label;
    private String iconKey;

    @NotNull
    private LearningStatus status;

    private String plainUnderstanding;
    private String todayFeeling;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public LearningStatus getStatus() {
        return status;
    }

    public void setStatus(LearningStatus status) {
        this.status = status;
    }

    public String getPlainUnderstanding() {
        return plainUnderstanding;
    }

    public void setPlainUnderstanding(String plainUnderstanding) {
        this.plainUnderstanding = plainUnderstanding;
    }

    public String getTodayFeeling() {
        return todayFeeling;
    }

    public void setTodayFeeling(String todayFeeling) {
        this.todayFeeling = todayFeeling;
    }
}
