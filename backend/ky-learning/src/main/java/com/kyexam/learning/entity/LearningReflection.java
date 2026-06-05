package com.kyexam.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("learning_reflection")
public class LearningReflection {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long userNodeId;

    private String contentType;

    private String content;

    private String agentAnswer;

    private LocalDateTime createdAt;

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

    public Long getUserNodeId() {
        return userNodeId;
    }

    public void setUserNodeId(Long userNodeId) {
        this.userNodeId = userNodeId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAgentAnswer() {
        return agentAnswer;
    }

    public void setAgentAnswer(String agentAnswer) {
        this.agentAnswer = agentAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
