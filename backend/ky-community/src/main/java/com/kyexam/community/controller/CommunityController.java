package com.kyexam.community.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.community.dto.CommunityPostCreateRequest;
import com.kyexam.community.dto.CommunityPostStatusRequest;
import com.kyexam.community.entity.CommunityPost;
import com.kyexam.community.service.CommunityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ApiResponse<List<CommunityPost>> posts() {
        return ApiResponse.ok(communityService.publishedPosts());
    }

    @PostMapping("/posts")
    public ApiResponse<CommunityPost> createPost(@Validated @RequestBody CommunityPostCreateRequest request) {
        return ApiResponse.ok(communityService.createPost(request));
    }

    @GetMapping("/admin/posts")
    public ApiResponse<List<CommunityPost>> managedPosts() {
        return ApiResponse.ok(communityService.managedPosts());
    }

    @PutMapping("/admin/posts/{id}/status")
    public ApiResponse<CommunityPost> updateStatus(
            @PathVariable Long id,
            @Validated @RequestBody CommunityPostStatusRequest request) {
        return ApiResponse.ok(communityService.updateStatus(id, request));
    }
}
