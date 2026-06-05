package com.kyexam.learning.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.learning.dto.LearningNodeConnectionRequest;
import com.kyexam.learning.dto.LearningNodeConnectionView;
import com.kyexam.learning.dto.LearningNodeCreateRequest;
import com.kyexam.learning.dto.LearningNodeIconRequest;
import com.kyexam.learning.dto.LearningNodeTagRequest;
import com.kyexam.learning.dto.LearningNodeTagView;
import com.kyexam.learning.dto.LearningNodeTree;
import com.kyexam.learning.dto.LearningNodeUpdateRequest;
import com.kyexam.learning.entity.UserSyllabusNode;
import com.kyexam.learning.service.LearningTreeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningTreeController {
    private final LearningTreeService learningTreeService;

    public LearningTreeController(LearningTreeService learningTreeService) {
        this.learningTreeService = learningTreeService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<LearningNodeTree>> tree(@RequestParam(required = false) Long treeId) {
        return ApiResponse.ok(learningTreeService.tree(treeId));
    }

    @PostMapping("/import-official")
    public ApiResponse<List<LearningNodeTree>> importOfficialTree(@RequestParam(required = false) Long treeId) {
        return ApiResponse.ok(learningTreeService.importOfficialTree(treeId));
    }

    @PostMapping("/nodes")
    public ApiResponse<UserSyllabusNode> createCustomNode(@Validated @RequestBody LearningNodeCreateRequest request) {
        return ApiResponse.ok(learningTreeService.createCustomNode(request));
    }

    @PutMapping("/nodes/{id}")
    public ApiResponse<UserSyllabusNode> updateNode(
            @PathVariable Long id,
            @Validated @RequestBody LearningNodeUpdateRequest request) {
        return ApiResponse.ok(learningTreeService.updateNode(id, request));
    }

    @PutMapping("/nodes/{id}/icon")
    public ApiResponse<UserSyllabusNode> updateNodeIcon(
            @PathVariable Long id,
            @RequestBody LearningNodeIconRequest request) {
        return ApiResponse.ok(learningTreeService.updateNodeIcon(id, request.getIconKey()));
    }

    @PostMapping("/nodes/{id}/review")
    public ApiResponse<UserSyllabusNode> review(@PathVariable Long id) {
        return ApiResponse.ok(learningTreeService.review(id));
    }

    @GetMapping("/nodes/{id}/tags")
    public ApiResponse<List<LearningNodeTagView>> tags(@PathVariable Long id) {
        return ApiResponse.ok(learningTreeService.tags(id));
    }

    @PostMapping("/nodes/{id}/tags")
    public ApiResponse<LearningNodeTagView> createTag(
            @PathVariable Long id,
            @Validated @RequestBody LearningNodeTagRequest request) {
        return ApiResponse.ok(learningTreeService.createTag(id, request));
    }

    @PutMapping("/tags/{tagId}")
    public ApiResponse<LearningNodeTagView> updateTag(
            @PathVariable Long tagId,
            @Validated @RequestBody LearningNodeTagRequest request) {
        return ApiResponse.ok(learningTreeService.updateTag(tagId, request));
    }

    @DeleteMapping("/tags/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Long tagId) {
        learningTreeService.deleteTag(tagId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/connections")
    public ApiResponse<List<LearningNodeConnectionView>> connections() {
        return ApiResponse.ok(learningTreeService.connections());
    }

    @PostMapping("/connections")
    public ApiResponse<LearningNodeConnectionView> createConnection(
            @Validated @RequestBody LearningNodeConnectionRequest request) {
        return ApiResponse.ok(learningTreeService.createConnection(request));
    }

    @PutMapping("/connections/{connectionId}")
    public ApiResponse<LearningNodeConnectionView> updateConnection(
            @PathVariable Long connectionId,
            @Validated @RequestBody LearningNodeConnectionRequest request) {
        return ApiResponse.ok(learningTreeService.updateConnection(connectionId, request));
    }

    @DeleteMapping("/connections/{connectionId}")
    public ApiResponse<Void> deleteConnection(@PathVariable Long connectionId) {
        learningTreeService.deleteConnection(connectionId);
        return ApiResponse.ok(null);
    }
}
