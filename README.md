# 考研辅助知识树系统

一个面向考研复习过程管理的全栈系统。当前技术栈：

- 后端：Java 8 + Spring Boot 2.7 + MyBatis-Plus
- 前端：Vue 3 + TypeScript
- 基础设施：MySQL 8.0 / Redis 7（Docker）
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
├─ agent-service
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

当前 `docker-compose.yml` 使用 `mysql:8.0`。如果你本地已经存在旧的 MySQL 8.4 容器和数据卷，只有重建容器/数据卷后镜像版本才会切换；不重建时，现有数据库会继续保持原版本运行。

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

数据库 SQL 只保留一份初始化文件：

- `docker/mysql/init/001_schema.sql`
  - 只负责全新 MySQL 容器首次初始化。
  - 包含基础账号、RBAC、官方大纲、考研树、学习节点、标签、连接、反思、社区帖子等开发演示数据。
  - 后续如果有数据库结构或演示数据变更，直接更新这一份 SQL，并由我同步执行到当前本地 MySQL。

### 首次建库

Docker 官方 MySQL 镜像只会在数据目录为空时执行 `/docker-entrypoint-initdb.d`。如需重建演示库：

```powershell
docker compose down
docker volume rm ky-examination_ky_exam_mysql_data
docker compose up -d mysql redis
```

初始化完成后，普通考生 `candidate` 名下会得到 `2` 个分组、`7` 棵主要考研树，并包含节点标签、知识连接、学习反思、社区帖子等演示数据。

### 增量更新

项目不再使用数据库迁移工具。已有 `ky_examination` 库需要更新时，直接执行对应 SQL 到当前 MySQL；执行前建议先备份数据库。

## 学习树模型与删除策略

核心模型：

- `tree_group`：树分组，例如“专业课 / 公共课 / 自定义分组”。
- `study_tree`：一棵具体考研树，例如“数据结构 / 操作系统 / 高等数学 / 英语 / 政治”。
- `user_syllabus_node.tree_id`：学习节点归属到某棵考研树。
- `learning_node_tag`：节点标签。
- `learning_node_connection`：节点之间的知识连接。

删除策略：

- 删除分组前，如果分组下仍有树，会返回业务错误；需要先移动或删除这些树。
- 删除树时，在事务内删除该树下节点、节点标签、学习反思，以及涉及这些节点的知识连接，再删除树实例。
- 删除节点时，在事务内删除该节点子树、相关标签、反思和起点/终点涉及该子树的连接。

树本身不是 `user_syllabus_node`。在树根下新增一级节点时，`parentId` 传 `null`，并且必须传 `treeId`：

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
- `GET /api/study-groups/{id}`
- `POST /api/study-groups`
- `PUT /api/study-groups/{id}`
- `DELETE /api/study-groups/{id}`
- `GET /api/study-trees`
- `GET /api/study-trees/{id}`
- `GET /api/study-trees/{id}/children`
- `POST /api/study-trees`
- `PUT /api/study-trees/{id}`
- `DELETE /api/study-trees/{id}`
- `GET /api/learning/tree?treeId=1`
- `POST /api/learning/import-official?treeId=1`
- `POST /api/learning/nodes`
- `GET /api/learning/nodes/{id}`
- `PUT /api/learning/nodes/{id}`
- `DELETE /api/learning/nodes/{id}`
- `PUT /api/learning/nodes/{id}/icon`
- `POST /api/learning/nodes/{id}/review`
- `GET /api/learning/nodes/{id}/tags`
- `POST /api/learning/nodes/{id}/tags`
- `GET /api/learning/tags/{tagId}`
- `PUT /api/learning/tags/{tagId}`
- `DELETE /api/learning/tags/{tagId}`
- `GET /api/learning/connections?treeId=1`
- `GET /api/learning/connections/{connectionId}`
- `POST /api/learning/connections`
- `PUT /api/learning/connections/{connectionId}`
- `DELETE /api/learning/connections/{connectionId}`
- `GET /api/community/posts`
- `POST /api/community/posts`
- `GET /api/community/admin/posts`
- `PUT /api/community/admin/posts/{id}/status`
- `POST /api/agent/assist`

## 前端接入提示

- 切树：先调用 `GET /api/study-trees` 获取当前用户树列表，再把选中的 `tree.id` 传给 `GET /api/learning/tree?treeId=...`、`GET /api/learning/connections?treeId=...`。
- 树根下新增一级节点：调用 `POST /api/learning/nodes`，请求体带 `treeId` 且 `parentId: null`。
- 分组删除：如果后端返回“该分组下仍有考研树”，前端应提示用户先移动或删除分组内树。
- 树删除：删除会级联清理该树下节点、标签、反思和知识连接，前端建议做二次确认。

## 下一步建议

1. 继续补节点标签 CRUD 的前端操作入口
2. 接入节点连接的可视化创建与编辑
3. 给树节点增加拖拽排序与位置记忆
4. 把 Agent 区域真正接到独立 NestJS 服务
