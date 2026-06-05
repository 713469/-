package com.kyexam.common.enums;

public enum LearningStatus {
    NOT_STARTED("未开始"),
    IN_PROGRESS("学习中"),
    MASTERED("已掌握");

    private final String text;

    LearningStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
