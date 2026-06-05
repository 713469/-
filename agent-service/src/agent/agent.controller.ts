import {
  Body,
  Controller,
  MessageEvent,
  Post,
  Query,
  Sse,
} from '@nestjs/common';
import { Observable, concatMap, from, interval, map, take } from 'rxjs';
import { AgentService } from './agent.service';
import { AssistRequestDto } from './dto/assist-request.dto';
import { AssistResponseDto } from './dto/assist-response.dto';

@Controller('agent')
export class AgentController {
  constructor(private readonly agentService: AgentService) {}

  @Post('assist')
  assist(@Body() request: AssistRequestDto): AssistResponseDto {
    return this.agentService.assist(request);
  }

  @Sse('stream')
  stream(
    @Query('nodeId') nodeId = 'demo-node',
    @Query('feeling') feeling = '我有点不确定',
    @Query('plainUnderstanding') plainUnderstanding = '',
  ): Observable<MessageEvent> {
    const messages = this.agentService.createStreamMessages({
      nodeId,
      feeling,
      plainUnderstanding,
    });

    return from(messages).pipe(
      concatMap((message, index) =>
        interval(500).pipe(
          take(1),
          map(() => ({
            type: index === messages.length - 1 ? 'done' : 'message',
            data: { index, message },
          })),
        ),
      ),
    );
  }
}
