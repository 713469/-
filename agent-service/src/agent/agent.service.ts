import { Injectable } from '@nestjs/common';
import { AssistRequestDto } from './dto/assist-request.dto';
import { AssistResponseDto } from './dto/assist-response.dto';
import { MockKnowledgeService } from './mock/mock-knowledge.service';
import { MockMemoryService } from './mock/mock-memory.service';
import { MockWeaknessService } from './mock/mock-weakness.service';
import {
  MemorySummary,
  RetrievedKnowledge,
  WeaknessAssessment,
} from './types/agent-context';

@Injectable()
export class AgentService {
  constructor(
    private readonly knowledgeService: MockKnowledgeService,
    private readonly memoryService: MockMemoryService,
    private readonly weaknessService: MockWeaknessService,
  ) {}

  assist(request: AssistRequestDto): AssistResponseDto {
    const knowledge = this.knowledgeService.retrieve(request);
    const memory = this.memoryService.summarize(request);
    const weakness = this.weaknessService.assess(request);

    return {
      answer: this.composeAnswer(request, knowledge, memory, weakness),
      weaknessHints: weakness.hints,
      nextAction: this.composeNextAction(weakness, knowledge),
    };
  }

  createStreamMessages(request: AssistRequestDto): string[] {
    const response = this.assist(request);

    return [
      '已收到你的学习感受，正在读取节点上下文。',
      response.answer,
      `薄弱点提示：${response.weaknessHints.join('；')}`,
      `下一步：${response.nextAction}`,
    ];
  }

  private composeAnswer(
    request: AssistRequestDto,
    knowledge: RetrievedKnowledge[],
    memory: MemorySummary,
    weakness: WeaknessAssessment,
  ): string {
    const topKnowledge = knowledge[0];
    const understanding = request.plainUnderstanding?.trim();
    const userSummary = understanding
      ? `你现在的理解是：“${understanding}”。`
      : '你还没有写下完整理解，可以先从一句朴素解释开始。';
    const memoryHint =
      memory.recentConfusions.length > 0
        ? `结合长期记忆摘要，你最近更容易卡在${memory.recentConfusions.join('、')}。`
        : '从长期记忆摘要看，当前更适合用轻量追问来巩固。';

    return [
      `我读到你对节点 ${request.nodeId} 的感受是：“${request.feeling}”。`,
      userSummary,
      `mock 知识库最相关的提示是：${topKnowledge.title} - ${topKnowledge.content}`,
      `${memoryHint} 这次建议先按“概念是什么、为什么成立、题目里怎么识别”三步整理。`,
      `当前弱点等级为 ${weakness.severity}，先处理最容易补齐的一处即可。`,
    ].join('');
  }

  private composeNextAction(
    weakness: WeaknessAssessment,
    knowledge: RetrievedKnowledge[],
  ): string {
    if (weakness.severity === 'high') {
      return '先用 3 句话重写这个知识点：定义、原因、例子各一句。';
    }

    if (weakness.severity === 'medium') {
      return `围绕“${knowledge[0].title}”找一道基础题，标出题干里的触发词。`;
    }

    return '做一道相邻知识点的小题，检查能否把理解迁移过去。';
  }
}
