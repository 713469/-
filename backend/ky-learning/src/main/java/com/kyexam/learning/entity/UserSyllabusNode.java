package com.kyexam.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kyexam.common.enums.LearningStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("user_syllabus_node")
public class UserSyllabusNode {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long treeId;

    private Long officialNodeId;

    private Long parentId;

    private String title;

    private String label;

    private String iconKey;

    private Integer levelNo;

    private Integer sortOrder;

    private LearningStatus status;

    private Integer reviewCount;

    private String plainUnderstanding;

    private String todayFeeling;

    private Boolean customNode;

    private BigDecimal weakScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTreeId() {
        return treeId;
    }

    public void setTreeId(Long treeId) {
        this.treeId = treeId;
    }

    public Long getOfficialNodeId() {
        return officialNodeId;
    }

    public void setOfficialNodeId(Long officialNodeId) {
        this.officialNodeId = officialNodeId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
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

    public LearningStatus getStatus() {
        return status;
    }

    public void setStatus(LearningStatus status) {
        this.status = status;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
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

    public Boolean getCustomNode() {
        return customNode;
    }

    public void setCustomNode(Boolean customNode) {
        this.customNode = customNode;
    }

    public BigDecimal getWeakScore() {
        return weakScore;
    }

    public void setWeakScore(BigDecimal weakScore) {
        this.weakScore = weakScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
