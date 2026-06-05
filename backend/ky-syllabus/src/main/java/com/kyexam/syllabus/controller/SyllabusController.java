package com.kyexam.syllabus.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.syllabus.dto.SyllabusNodeRequest;
import com.kyexam.syllabus.dto.SyllabusNodeTree;
import com.kyexam.syllabus.entity.SyllabusNode;
import com.kyexam.syllabus.service.SyllabusService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {
    private final SyllabusService syllabusService;

    public SyllabusController(SyllabusService syllabusService) {
        this.syllabusService = syllabusService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<SyllabusNodeTree>> tree() {
        return ApiResponse.ok(syllabusService.tree());
    }

    @GetMapping("/children")
    public ApiResponse<List<SyllabusNodeTree>> children(@RequestParam Long parentId) {
        return ApiResponse.ok(syllabusService.children(parentId));
    }

    @PostMapping("/nodes")
    public ApiResponse<SyllabusNode> saveNode(@Validated @RequestBody SyllabusNodeRequest request) {
        return ApiResponse.ok(syllabusService.saveNode(request));
    }
}
