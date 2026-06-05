SET NAMES utf8mb4;

ALTER TABLE user_syllabus_node
  ADD COLUMN icon_key VARCHAR(64) NULL AFTER label;

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
