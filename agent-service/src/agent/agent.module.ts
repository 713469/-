import { Module } from '@nestjs/common';
import { AgentController } from './agent.controller';
import { AgentService } from './agent.service';
import { MockKnowledgeService } from './mock/mock-knowledge.service';
import { MockMemoryService } from './mock/mock-memory.service';
import { MockWeaknessService } from './mock/mock-weakness.service';

@Module({
  controllers: [AgentController],
  providers: [
    AgentService,
    MockKnowledgeService,
    MockMemoryService,
    MockWeaknessService,
  ],
})
export class AgentModule {}
