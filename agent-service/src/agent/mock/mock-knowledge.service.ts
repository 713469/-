import { Injectable } from '@nestjs/common';
import { AssistRequestDto } from '../dto/assist-request.dto';
import { RetrievedKnowledge } from '../types/agent-context';

const MOCK_KNOWLEDGE_BASE: Record<string, RetrievedKnowledge[]> = {
  default: [
    {
      title: '复述优先',
      content: '先用自己的话复述概念，再补充例子，可以暴露理解断点。',
      score: 0.72,
    },
    {
      title: '错因定位',
      content: '把不确定处拆成术语、条件、推导、应用四类，更容易找到薄弱点。',
      score: 0.66,
    },
  ],
  algorithm: [
    {
      title: '算法复杂度',
      content: '分析算法时要区分输入规模、循环层级、递归分支和额外空间。',
      score: 0.83,
    },
  ],
  database: [
    {
      title: '数据库事务',
      content: '事务题通常围绕 ACID、隔离级别、并发异常和锁机制展开。',
      score: 0.81,
    },
  ],
};

@Injectable()
export class MockKnowledgeService {
  retrieve(request: AssistRequestDto): RetrievedKnowledge[] {
    const text = `${request.nodeId} ${request.feeling} ${request.plainUnderstanding ?? ''}`;
    const topicKey = this.detectTopic(text);

    return [
      ...(MOCK_KNOWLEDGE_BASE[topicKey] ?? []),
      ...MOCK_KNOWLEDGE_BASE.default,
    ].slice(0, 3);
  }

  private detectTopic(text: string): string {
    const normalized = text.toLowerCase();

    if (/(算法|algorithm|复杂度|递归|排序)/i.test(normalized)) {
      return 'algorithm';
    }

    if (/(数据库|database|sql|事务|索引)/i.test(normalized)) {
      return 'database';
    }

    return 'default';
  }
}
