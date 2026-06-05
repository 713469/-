package com.kyexam.learning.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.learning.dto.TreeGroupRequest;
import com.kyexam.learning.entity.TreeGroup;
import com.kyexam.learning.service.StudyTreeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/study-groups")
public class StudyGroupController {
    private final StudyTreeService studyTreeService;

    public StudyGroupController(StudyTreeService studyTreeService) {
        this.studyTreeService = studyTreeService;
    }

    @GetMapping
    public ApiResponse<List<TreeGroup>> groups() {
        return ApiResponse.ok(studyTreeService.groups());
    }

    @GetMapping("/{id}")
    public ApiResponse<TreeGroup> group(@PathVariable Long id) {
        return ApiResponse.ok(studyTreeService.group(id));
    }

    @PostMapping
    public ApiResponse<TreeGroup> createGroup(@Validated @RequestBody TreeGroupRequest request) {
        return ApiResponse.ok(studyTreeService.createGroup(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TreeGroup> updateGroup(
            @PathVariable Long id,
            @Validated @RequestBody TreeGroupRequest request) {
        return ApiResponse.ok(studyTreeService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        studyTreeService.deleteGroup(id);
        return ApiResponse.ok(null);
    }
}
