package com.kyexam.agent.service;

import com.kyexam.agent.dto.AgentAssistRequest;
import com.kyexam.agent.dto.AgentAssistResponse;
import com.kyexam.learning.service.LearningTreeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AgentBridgeService {
    private final LearningTreeService learningTreeService;

    @Value("${ky.agent.base-url:http://localhost:3100}")
    private String agentBaseUrl;

    public AgentBridgeService(LearningTreeService learningTreeService) {
        this.learningTreeService = learningTreeService;
    }

    public AgentAssistResponse assist(AgentAssistRequest request) {
        learningTreeService.requireCurrentUserNode(request.getNodeId());

        AgentAssistResponse response = new AgentAssistResponse();
        response.setAnswer("Agent 服务暂未接入。当前桥接层已收到你的感受：\"" + request.getFeeling()
                + "\"。后续会把该节点上下文发往 " + agentBaseUrl + "，结合 408 RAG 知识库生成解释。");
        response.setWeaknessHints(Arrays.asList("PV 操作语义", "同步互斥建模", "题目条件到信号量初值的转换"));
        response.setNextAction("先把等待条件、互斥资源和执行顺序分别用三列写出来，再决定 P/V 放置位置。");
        return response;
    }
}
