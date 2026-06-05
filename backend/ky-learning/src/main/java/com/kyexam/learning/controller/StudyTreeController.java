package com.kyexam.learning.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.learning.dto.LearningNodeTree;
import com.kyexam.learning.dto.StudyTreeRequest;
import com.kyexam.learning.entity.StudyTree;
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
@RequestMapping("/api/study-trees")
public class StudyTreeController {
    private final StudyTreeService studyTreeService;

    public StudyTreeController(StudyTreeService studyTreeService) {
        this.studyTreeService = studyTreeService;
    }

    @GetMapping
    public ApiResponse<List<StudyTree>> trees() {
        return ApiResponse.ok(studyTreeService.trees());
    }

    @PostMapping
    public ApiResponse<StudyTree> createTree(@Validated @RequestBody StudyTreeRequest request) {
        return ApiResponse.ok(studyTreeService.createTree(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudyTree> updateTree(
            @PathVariable Long id,
            @Validated @RequestBody StudyTreeRequest request) {
        return ApiResponse.ok(studyTreeService.updateTree(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTree(@PathVariable Long id) {
        studyTreeService.deleteTree(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<StudyTree> tree(@PathVariable Long id) {
        return ApiResponse.ok(studyTreeService.tree(id));
    }

    @GetMapping("/{id}/children")
    public ApiResponse<List<LearningNodeTree>> rootChildren(@PathVariable Long id) {
        return ApiResponse.ok(studyTreeService.rootChildren(id));
    }
}
