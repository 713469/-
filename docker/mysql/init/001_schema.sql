SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  display_name VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY,
  code VARCHAR(48) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS syllabus_node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NULL,
  code VARCHAR(96) NOT NULL UNIQUE,
  title VARCHAR(160) NOT NULL,
  label VARCHAR(64) NOT NULL,
  level_no INT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  description VARCHAR(1000),
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_syllabus_parent_sort(parent_id, sort_order),
  CONSTRAINT fk_syllabus_parent FOREIGN KEY (parent_id) REFERENCES syllabus_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_syllabus_node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  tree_id BIGINT NULL,
  official_node_id BIGINT NULL,
  parent_id BIGINT NULL,
  title VARCHAR(160) NOT NULL,
  label VARCHAR(64) NOT NULL,
  icon_key VARCHAR(64) NULL,
  level_no INT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
  review_count INT NOT NULL DEFAULT 0,
  plain_understanding TEXT,
  today_feeling TEXT,
  custom_node TINYINT(1) NOT NULL DEFAULT 0,
  weak_score DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_study_tree(user_id, tree_id, parent_id, sort_order),
  INDEX idx_user_tree_parent(user_id, parent_id, sort_order),
  INDEX idx_user_tree_official(user_id, official_node_id),
  CONSTRAINT fk_user_tree_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_tree_parent FOREIGN KEY (parent_id) REFERENCES user_syllabus_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tree_group (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(1000),
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_tree_group_user_sort(user_id, sort_order, id),
  CONSTRAINT fk_tree_group_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS study_tree (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  group_id BIGINT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(1000),
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_study_tree_user_sort(user_id, sort_order, id),
  INDEX idx_study_tree_group(group_id, sort_order, id),
  CONSTRAINT fk_study_tree_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_study_tree_group FOREIGN KEY (group_id) REFERENCES tree_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS learning_reflection (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  user_node_id BIGINT NOT NULL,
  content_type VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  agent_answer TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_reflection_user_node(user_id, user_node_id, created_at),
  CONSTRAINT fk_reflection_node FOREIGN KEY (user_node_id) REFERENCES user_syllabus_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS community_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_post_status_time(status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_role(id, code, name, description) VALUES
  (1, 'SYSTEM_ADMIN', '系统管理员', '维护官方 408 大纲树与系统底层数据'),
  (2, 'COMMUNITY_ADMIN', '社区管理员', '管理资料库与公共讨论区'),
  (3, 'USER', '11408 考生', '使用个人学习看板与反思笔记')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO sys_user(id, username, display_name, password_hash, enabled) VALUES
  (1, 'root', '系统管理员', '{noop}admin123', 1),
  (2, 'community', '社区管理员', '{noop}admin123', 1),
  (10001, 'candidate', '408 考生', '{noop}user123', 1)
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), enabled = VALUES(enabled);

INSERT IGNORE INTO sys_user_role(user_id, role_id) VALUES
  (1, 1),
  (2, 2),
  (10001, 3);

INSERT INTO tree_group(user_id, name, description, sort_order)
SELECT u.id, '专业课', '考研专业课知识树分组', 10
FROM sys_user u
WHERE NOT EXISTS (
  SELECT 1 FROM tree_group g WHERE g.user_id = u.id AND g.name = '专业课'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT u.id, g.id, '408', '默认考研专业课学习树', 10
FROM sys_user u
JOIN tree_group g ON g.user_id = u.id AND g.name = '专业课'
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree t WHERE t.user_id = u.id
);

INSERT INTO syllabus_node(id, parent_id, code, title, label, level_no, sort_order, description) VALUES
  (1, NULL, 'CS', '计算机组成原理', '科目', 1, 10, '11408 四科之一，关注硬件系统与组成结构'),
  (2, NULL, 'DS', '数据结构', '科目', 1, 20, '11408 四科之一，关注抽象数据类型与算法基础'),
  (3, NULL, 'OS', '操作系统', '科目', 1, 30, '11408 四科之一，关注进程、内存、文件与 I/O'),
  (4, NULL, 'CN', '计算机网络', '科目', 1, 40, '11408 四科之一，关注分层协议与网络应用'),
  (10, 3, 'OS-PROCESS', '进程与线程', '章', 2, 10, '进程概念、状态转换、调度与同步互斥'),
  (11, 10, 'OS-PROCESS-SYNC', '同步与互斥', '节', 3, 10, '信号量、管程、经典同步问题'),
  (12, 11, 'OS-PV', 'PV 操作与信号量', '考点', 4, 10, 'P/V 原语、互斥信号量与同步信号量的使用'),
  (13, 11, 'OS-DEADLOCK', '死锁', '考点', 4, 20, '死锁条件、预防、避免、检测与解除'),
  (20, 2, 'DS-LINEAR', '线性表', '章', 2, 10, '顺序表、链表及相关操作'),
  (21, 20, 'DS-LIST', '链表', '节', 3, 10, '单链表、双链表、循环链表'),
  (22, 21, 'DS-LIST-REVERSE', '链表逆置', '考点', 4, 10, '头插法、递归、指针调整边界'),
  (30, 4, 'CN-TRANSPORT', '传输层', '章', 2, 10, 'TCP/UDP、可靠传输、拥塞控制'),
  (31, 30, 'CN-TCP', 'TCP 协议', '节', 3, 10, '连接管理、可靠传输、流量控制'),
  (32, 31, 'CN-TCP-CONGESTION', 'TCP 拥塞控制', '考点', 4, 10, '慢开始、拥塞避免、快重传与快恢复'),
  (40, 1, 'CS-MEMORY', '存储系统', '章', 2, 10, '层次化存储、Cache、虚拟存储器'),
  (41, 40, 'CS-CACHE', 'Cache', '节', 3, 10, '映射方式、替换策略、命中率计算')
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  title = VALUES(title),
  label = VALUES(label),
  level_no = VALUES(level_no),
  sort_order = VALUES(sort_order),
  description = VALUES(description);
