CREATE TABLE IF NOT EXISTS `site_message` (
  `id` VARCHAR(64) NOT NULL COMMENT '站内消息ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(150) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息正文',
  `category` VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '消息分类',
  `link` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
  `is_read` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
  `creator_id` VARCHAR(64) DEFAULT NULL COMMENT '发送人ID',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_site_message_user_id` (`user_id`),
  KEY `idx_site_message_user_read` (`user_id`, `is_read`),
  KEY `idx_site_message_create_time` (`create_time`),
  CONSTRAINT `fk_site_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';
