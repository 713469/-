package com.kyexam.learning.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TreeGroupRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 1000)
    private String description;

    private Integer sortOrder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
