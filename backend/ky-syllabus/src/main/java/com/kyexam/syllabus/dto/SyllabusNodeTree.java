package com.kyexam.syllabus.dto;

import java.util.ArrayList;
import java.util.List;

public class SyllabusNodeTree {
    private Long id;
    private Long parentId;
    private String code;
    private String title;
    private String label;
    private Integer levelNo;
    private Integer sortOrder;
    private String description;
    private List<SyllabusNodeTree> children = new ArrayList<SyllabusNodeTree>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    public Integer getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(Integer levelNo) {
        this.levelNo = levelNo;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SyllabusNodeTree> getChildren() {
        return children;
    }

    public void setChildren(List<SyllabusNodeTree> children) {
        this.children = children;
    }
}
