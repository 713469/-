# ky-examination Agent Service

独立运行的 Agent 微服务骨架，技术栈为 TypeScript + NestJS。当前只提供 mock 版的知识库检索、弱点判断和长期记忆摘要，后续可以在相同接口下替换为真实 RAG、Memory 和模型调用。

## 目录结构

```text
agent-service/
  src/
    agent/
      dto/
      mock/
      types/
      agent.controller.ts
      agent.service.ts
      agent.module.ts
    health/
    app.module.ts
    main.ts
```

## 启动

```bash
cd agent-service
npm install
npm run start:dev
```

默认端口为 `4100`，可通过 `.env` 覆盖：

```bash
cp .env.example .env
```

```env
AGENT_SERVICE_PORT=4100
CORS_ORIGIN=http://localhost:5173
```

## HTTP 接口

### POST /agent/assist

当前前端期望的最小兼容结构：

```json
{
  "nodeId": "database-transaction",
  "feeling": "我大概懂事务，但隔离级别还是有点混淆",
  "plainUnderstanding": "事务就是把一组数据库操作当成一个整体执行"
}
```

响应：

```json
{
  "answer": "string",
  "weaknessHints": ["string"],
  "nextAction": "string"
}
```

### GET /agent/stream

SSE 示例接口，为后续流式输出预留。

示例：

```text
GET http://localhost:4100/agent/stream?nodeId=database-transaction&feeling=有点混淆&plainUnderstanding=事务是一组操作
```

### GET /health

健康检查：

```json
{
  "status": "ok",
  "service": "ky-examination-agent-service",
  "timestamp": "2026-06-05T00:00:00.000Z"
}
```

## 与 Spring Boot 对接建议

主后端已有 `/api/agent/assist` 占位接口时，建议由 Spring Boot 代理调用本服务：

```text
AGENT_SERVICE_BASE_URL=http://localhost:4100
POST ${AGENT_SERVICE_BASE_URL}/agent/assist
```

这样前端继续请求现有主后端 `/api/agent/assist`，主业务后端只负责鉴权、用户上下文和转发，真实 Agent 能力独立部署在本服务内。

## 后续接入点

- `MockKnowledgeService`：替换为向量检索、文档召回或知识图谱检索。
- `MockWeaknessService`：替换为基于答题记录、错题标签和学习路径的弱点判断。
- `MockMemoryService`：替换为用户长期记忆摘要、学习偏好和历史困惑管理。
- `AgentService`：保留响应结构，接入真实 LLM 编排、prompt 模板和工具调用。
