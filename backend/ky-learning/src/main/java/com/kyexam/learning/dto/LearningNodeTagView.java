package com.kyexam.learning.dto;

public class LearningNodeTagView {
    private Long id;
    private Long userNodeId;
    private String name;
    private String color;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserNodeId() {
        return userNodeId;
    }

    public void setUserNodeId(Long userNodeId) {
        this.userNodeId = userNodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
