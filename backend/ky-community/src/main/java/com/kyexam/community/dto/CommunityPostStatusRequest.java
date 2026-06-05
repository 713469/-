package com.kyexam.community.dto;

import javax.validation.constraints.NotBlank;

public class CommunityPostStatusRequest {
    @NotBlank
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
