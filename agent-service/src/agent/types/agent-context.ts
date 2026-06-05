export interface RetrievedKnowledge {
  title: string;
  content: string;
  score: number;
}

export interface MemorySummary {
  learnerPattern: string;
  recentConfusions: string[];
  suggestedTone: 'encouraging' | 'precise' | 'step-by-step';
}

export interface WeaknessAssessment {
  hints: string[];
  severity: 'low' | 'medium' | 'high';
}
