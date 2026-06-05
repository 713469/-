import { Controller, Get } from '@nestjs/common';

@Controller('health')
export class HealthController {
  @Get()
  check() {
    return {
      status: 'ok',
      service: 'ky-examination-agent-service',
      timestamp: new Date().toISOString(),
    };
  }
}
