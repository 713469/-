import { Injectable } from '@nestjs/common';
import { AssistRequestDto } from '../dto/assist-request.dto';
import { MemorySummary } from '../types/agent-context';

@Injectable()
export class MockMemoryService {
  summarize(request: AssistRequestDto): MemorySummary {
    const combinedText = `${request.feeling} ${request.plainUnderstanding ?? ''}`;
    const recentConfusions = this.extractConfusions(combinedText);

    return {
      learnerPattern:
        '学习者倾向于先表达直觉感受，再尝试用朴素语言解释概念。',
      recentConfusions,
      suggestedTone: recentConfusions.length >= 2 ? 'step-by-step' : 'encouraging',
    };
  }

  private extractConfusions(text: string): string[] {
    const candidates = [
      { pattern: /(记不住|容易忘|混淆)/, label: '概念记忆不稳定' },
      { pattern: /(不会用|题目|应用|例子)/, label: '应用场景迁移不足' },
      { pattern: /(为什么|原理|本质|底层)/, label: '原理链路还不完整' },
      { pattern: /(区别|对比|差别)/, label: '相近概念边界不清' },
    ];

    return candidates
      .filter((candidate) => candidate.pattern.test(text))
      .map((candidate) => candidate.label)
      .slice(0, 3);
  }
}
