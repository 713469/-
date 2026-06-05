# 考研辅助知识树系统

一个面向考研复习过程管理的全栈系统。当前技术栈：

- 后端：Java 8 + Spring Boot 2.7 + MyBatis-Plus
- 前端：Vue 3 + TypeScript
- 基础设施：MySQL 8 / Redis 7（Docker）
- 后续预留：独立运行的 NestJS Agent 服务

当前重点能力：

- RBAC 登录与角色区分
- 多考研树 / 多分组
- 官方大纲树导入
- 用户私有学习树与节点编辑
- 节点状态、复习次数、薄弱度、标签、连接

## 目录结构

```text
.
├─ backend
│  ├─ ky-admin-app
│  ├─ ky-agent-bridge
│  ├─ ky-common
│  ├─ ky-community
│  ├─ ky-learning
│  ├─ ky-syllabus
│  └─ ky-system
├─ frontend
├─ docker
│  └─ mysql/init
└─ docker-compose.yml
```

## 启动 MySQL / Redis

项目只使用 Docker 启动数据库与缓存，不把前后端开发服务打进 Docker。

```powershell
docker compose up -d
```

默认端口：

- MySQL: `localhost:3307`
- Redis: `localhost:6379`

默认数据库信息：

- database: `ky_examination`
- user: `ky_user`
- password: `ky_password`
- root password: `ky_root_password`

## 后端启动

```powershell
cd backend
mvn -DskipTests install
cd ky-admin-app
mvn spring-boot:run
```

默认地址：

- 后端：`http://localhost:8080`

### IDEA 中启动后端

直接运行 `ky-admin-app` 模块里的启动类 `KyAdminApplication`。

项目已引入 `spring-boot-devtools`，建议在 IDEA 中开启：

1. `Settings -> Build, Execution, Deployment -> Compiler`
2. 勾选 `Build project automatically`
3. `Advanced Settings -> Compiler -> Allow auto-make to start even if developed application is currently running`

这样修改后端代码并触发编译后，Spring Boot 会自动重启。

## 前端启动

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

默认地址：

- 前端：`http://localhost:5173`

## 默认账号

```text
root / admin123              SYSTEM_ADMIN
community / admin123         COMMUNITY_ADMIN
candidate / user123         USER
```

登录成功后，前端会把 token 保存到 `localStorage`，后续请求自动携带：

```text
Authorization: Bearer <token>
```

## 数据库管理规则

现在数据库规则已经收束成两层：

1. `docker/mysql/init/001_schema.sql`
   - 只负责“新库首次初始化”的完整基线
   - 适合全新 MySQL 容器第一次启动时执行

2. `backend/ky-admin-app/src/main/resources/db/migration`
   - 只负责“后续结构演进”
   - 当前已启用 Flyway
   - 新增字段、新表、迁移逻辑，后续统一写到这里

### 当前约定

- 不再继续维护 `runtime-updates.sql`
- 不再把增量变更继续堆到 `docker/mysql/init/002_*.sql`
- 以后每一次数据库结构变更，都新增一个 Flyway 文件，例如：

```text
V3__add_xxx.sql
V4__create_xxx.sql
```

### 当前已存在的迁移

- `V2__learning_tree_extensions.sql`
  - 补齐 `user_syllabus_node.icon_key`
  - 补齐 `user_syllabus_node.tree_id`
  - 创建 `tree_group`
  - 创建 `study_tree`
  - 创建 `learning_node_tag`
  - 创建 `learning_node_connection`
  - 初始化默认 `专业课 / 408`
  - 把老节点回填到默认树

### 关于本地数据库落库

这次整理过程中，当前开发库 `ky_examination` 已经由我手动执行过迁移 SQL，不只是改了文件。

后续如果再有数据库结构更新，我会遵循这套流程：

1. 先新增 Flyway 迁移文件
2. 再把迁移实际执行到你当前使用的 MySQL
3. 最后同步更新 README 中的说明

## 多树模型说明

核心模型：

- `tree_group`：树分组，例如“专业课 / 公共课 / 自定义”
- `study_tree`：一棵具体的考研树，例如“408 / 高数 / 英语一”
- `user_syllabus_node.tree_id`：节点归属到哪一棵树

注意：

- 树本身不是 `user_syllabus_node`
- 根级新增节点时，`parentId` 应为 `null`
- 节点必须带 `treeId`

示例：

```http
POST /api/learning/nodes
Content-Type: application/json

{
  "treeId": 1,
  "parentId": null,
  "title": "高等数学",
  "label": "科目",
  "sortOrder": 10
}
```

## 常用接口

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/health`
- `GET /api/syllabus/tree`
- `POST /api/syllabus/nodes`
- `GET /api/study-groups`
- `POST /api/study-groups`
- `PUT /api/study-groups/{id}`
- `DELETE /api/study-groups/{id}`
- `GET /api/study-trees`
- `POST /api/study-trees`
- `PUT /api/study-trees/{id}`
- `DELETE /api/study-trees/{id}`
- `GET /api/study-trees/{id}`
- `GET /api/study-trees/{id}/children`
- `GET /api/learning/tree?treeId=1`
- `POST /api/learning/import-official?treeId=1`
- `POST /api/learning/nodes`
- `PUT /api/learning/nodes/{id}`
- `POST /api/learning/nodes/{id}/review`
- `GET /api/community/posts`
- `POST /api/community/posts`
- `GET /api/community/admin/posts`
- `PUT /api/community/admin/posts/{id}/status`
- `POST /api/agent/assist`

## 下一步建议

1. 继续补节点标签 CRUD 的前端操作入口
2. 接入节点连接的可视化创建与编辑
3. 给树节点增加拖拽排序与位置记忆
4. 把 Agent 区域真正接到独立 NestJS 服务
