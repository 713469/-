package com.kyexam.agent.controller;

import com.kyexam.agent.dto.AgentAssistRequest;
import com.kyexam.agent.dto.AgentAssistResponse;
import com.kyexam.agent.service.AgentBridgeService;
import com.kyexam.common.api.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentBridgeController {
    private final AgentBridgeService agentBridgeService;

    public AgentBridgeController(AgentBridgeService agentBridgeService) {
        this.agentBridgeService = agentBridgeService;
    }

    @PostMapping("/assist")
    public ApiResponse<AgentAssistResponse> assist(@Validated @RequestBody AgentAssistRequest request) {
        return ApiResponse.ok(agentBridgeService.assist(request));
    }
}
