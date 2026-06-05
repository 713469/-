SET @current_schema = DATABASE();

SET @icon_key_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @current_schema
    AND TABLE_NAME = 'user_syllabus_node'
    AND COLUMN_NAME = 'icon_key'
);
SET @icon_key_sql = IF(
  @icon_key_exists = 0,
  'ALTER TABLE user_syllabus_node ADD COLUMN icon_key VARCHAR(64) NULL AFTER label',
  'SELECT 1'
);
PREPARE stmt_icon_key FROM @icon_key_sql;
EXECUTE stmt_icon_key;
DEALLOCATE PREPARE stmt_icon_key;

SET @tree_id_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @current_schema
    AND TABLE_NAME = 'user_syllabus_node'
    AND COLUMN_NAME = 'tree_id'
);
SET @tree_id_sql = IF(
  @tree_id_exists = 0,
  'ALTER TABLE user_syllabus_node ADD COLUMN tree_id BIGINT NULL AFTER user_id',
  'SELECT 1'
);
PREPARE stmt_tree_id FROM @tree_id_sql;
EXECUTE stmt_tree_id;
DEALLOCATE PREPARE stmt_tree_id;

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

UPDATE user_syllabus_node n
JOIN study_tree t ON t.user_id = n.user_id
SET n.tree_id = t.id
WHERE n.tree_id IS NULL;

CREATE TABLE IF NOT EXISTS learning_node_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  user_node_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  color VARCHAR(32) NOT NULL DEFAULT '#6bfb9a',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_learning_node_tag_node(user_id, user_node_id, id),
  CONSTRAINT fk_learning_tag_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_learning_tag_node FOREIGN KEY (user_node_id) REFERENCES user_syllabus_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS learning_node_connection (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  source_node_id BIGINT NOT NULL,
  target_node_id BIGINT NOT NULL,
  relation_type VARCHAR(32) NOT NULL DEFAULT 'RELATED',
  label VARCHAR(160) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_learning_node_connection(user_id, source_node_id, target_node_id),
  INDEX idx_learning_node_connection_source(user_id, source_node_id, id),
  INDEX idx_learning_node_connection_target(user_id, target_node_id, id),
  CONSTRAINT fk_learning_connection_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_learning_connection_source FOREIGN KEY (source_node_id) REFERENCES user_syllabus_node(id),
  CONSTRAINT fk_learning_connection_target FOREIGN KEY (target_node_id) REFERENCES user_syllabus_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
