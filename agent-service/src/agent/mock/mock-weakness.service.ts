import { Injectable } from '@nestjs/common';
import { AssistRequestDto } from '../dto/assist-request.dto';
import { WeaknessAssessment } from '../types/agent-context';

@Injectable()
export class MockWeaknessService {
  assess(request: AssistRequestDto): WeaknessAssessment {
    const plainUnderstanding = request.plainUnderstanding?.trim() ?? '';
    const hints: string[] = [];

    if (plainUnderstanding.length < 20) {
      hints.push('当前解释偏短，可以补一句“它解决什么问题”。');
    }

    if (!/(因为|所以|导致|本质|原理)/.test(plainUnderstanding)) {
      hints.push('可以补充因果关系，避免只记结论。');
    }

    if (/(差不多|大概|有点|不确定|不会|懵)/.test(request.feeling)) {
      hints.push('主观感受里有不确定信号，建议先做概念边界对比。');
    }

    if (hints.length === 0) {
      hints.push('理解表达比较完整，下一步可以用一道题检验迁移能力。');
    }

    return {
      hints: hints.slice(0, 3),
      severity: hints.length >= 3 ? 'high' : hints.length === 2 ? 'medium' : 'low',
    };
  }
}
