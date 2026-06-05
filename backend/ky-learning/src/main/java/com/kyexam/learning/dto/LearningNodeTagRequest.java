package com.kyexam.learning.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LearningNodeTagRequest {
    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String color;

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
