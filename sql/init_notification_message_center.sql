CREATE TABLE IF NOT EXISTS `site_message` (
  `id` VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `content` TEXT NOT NULL,
  `category` VARCHAR(32) NOT NULL DEFAULT 'system',
  `link` VARCHAR(255) DEFAULT NULL,
  `is_read` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `read_time` DATETIME DEFAULT NULL,
  `creator_id` VARCHAR(64) DEFAULT NULL,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_site_message_user_id` (`user_id`),
  KEY `idx_site_message_user_read` (`user_id`, `is_read`),
  KEY `idx_site_message_create_time` (`create_time`),
  CONSTRAINT `fk_site_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `template` (
  `id` VARCHAR(64) NOT NULL,
  `template_code` VARCHAR(64) NOT NULL,
  `business_code` VARCHAR(64) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `channel` VARCHAR(32) NOT NULL,
  `content` JSON NOT NULL,
  `params_schema` JSON DEFAULT NULL,
  `version` INT NOT NULL DEFAULT 1,
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` VARCHAR(64) DEFAULT NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_template_business` (`business_code`, `channel`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `template_version` (
  `id` VARCHAR(64) NOT NULL,
  `template_id` VARCHAR(64) NOT NULL,
  `version` INT NOT NULL,
  `template_code` VARCHAR(64) NOT NULL,
  `business_code` VARCHAR(64) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `channel` VARCHAR(32) NOT NULL,
  `content` JSON NOT NULL,
  `params_schema` JSON DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL,
  `change_reason` VARCHAR(255) DEFAULT NULL,
  `created_by` VARCHAR(64) DEFAULT NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_version` (`template_id`, `version`),
  KEY `idx_template_version_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `carrier` (
  `id` VARCHAR(64) NOT NULL,
  `channel_name` VARCHAR(128) NOT NULL,
  `channel_type` VARCHAR(32) NOT NULL,
  `channel_provider` VARCHAR(64) NOT NULL,
  `config_json` JSON NOT NULL,
  `priority` INT NOT NULL DEFAULT 1,
  `weight` INT NOT NULL DEFAULT 100,
  `max_retry` INT NOT NULL DEFAULT 3,
  `remark` VARCHAR(255) DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'active',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_provider` (`channel_type`, `channel_provider`),
  KEY `idx_channel_status` (`channel_type`, `status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `message` (
  `id` VARCHAR(64) NOT NULL,
  `batch_id` VARCHAR(64) DEFAULT NULL,
  `request_id` VARCHAR(64) DEFAULT NULL,
  `policy_id` VARCHAR(64) DEFAULT NULL,
  `template_id` VARCHAR(64) DEFAULT NULL,
  `template_code` VARCHAR(64) DEFAULT NULL,
  `carrier_id` VARCHAR(64) DEFAULT NULL,
  `channel_type` VARCHAR(32) NOT NULL,
  `provider` VARCHAR(64) DEFAULT NULL,
  `business_code` VARCHAR(64) NOT NULL,
  `recipient_type` VARCHAR(32) NOT NULL,
  `recipient_value` VARCHAR(255) NOT NULL,
  `params_json` JSON DEFAULT NULL,
  `content_title` VARCHAR(255) DEFAULT NULL,
  `content_body` TEXT NOT NULL,
  `template_snapshot` JSON NOT NULL,
  `send_time_planned` DATETIME NOT NULL,
  `send_time_actual` DATETIME DEFAULT NULL,
  `read_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'pending',
  `retry_count` INT NOT NULL DEFAULT 0,
  `max_retry` INT NOT NULL DEFAULT 3,
  `next_retry_time` DATETIME DEFAULT NULL,
  `failure_reason` VARCHAR(500) DEFAULT NULL,
  `created_by` VARCHAR(64) DEFAULT NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `canceled` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_message_batch` (`batch_id`),
  KEY `idx_message_request` (`request_id`),
  KEY `idx_message_status` (`status`, `send_time_planned`),
  KEY `idx_message_business` (`business_code`, `channel_type`),
  KEY `idx_message_recipient` (`recipient_type`, `recipient_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `policy` (
  `id` VARCHAR(64) NOT NULL,
  `policy_name` VARCHAR(128) NOT NULL,
  `template_id` VARCHAR(64) DEFAULT NULL,
  `template_code` VARCHAR(64) DEFAULT NULL,
  `carrier_id` VARCHAR(64) DEFAULT NULL,
  `channel_type` VARCHAR(32) DEFAULT NULL,
  `business_code` VARCHAR(64) NOT NULL,
  `dynamic_condition` TEXT NOT NULL,
  `send_mode` VARCHAR(32) NOT NULL,
  `send_time` DATETIME DEFAULT NULL,
  `recurring_rule` VARCHAR(128) DEFAULT NULL,
  `recurring_end_time` DATETIME DEFAULT NULL,
  `priority` INT NOT NULL DEFAULT 5,
  `max_retry` INT NOT NULL DEFAULT 3,
  `status` VARCHAR(32) NOT NULL DEFAULT 'active',
  `created_by` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_policy_business` (`business_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `carrier`
    (`id`, `channel_name`, `channel_type`, `channel_provider`, `config_json`, `priority`, `weight`, `max_retry`, `remark`, `status`)
VALUES
    ('carrier-in-app-system', 'System In-App', 'in_app', 'system', JSON_OBJECT(), 1, 100, 3, 'Default in-app carrier', 'active'),
    ('carrier-email-spring', 'Spring Mail', 'email', 'spring_mail', JSON_OBJECT(), 1, 100, 3, 'Default spring mail carrier', 'active'),
    ('carrier-sms-mock', 'Mock SMS', 'sms', 'mock_sms', JSON_OBJECT(), 1, 100, 3, 'Mock sms carrier', 'disabled'),
    ('carrier-push-mock', 'Mock Push', 'push', 'mock_push', JSON_OBJECT(), 1, 100, 3, 'Mock push carrier', 'disabled');

INSERT IGNORE INTO `template`
    (`id`, `template_code`, `business_code`, `name`, `channel`, `content`, `params_schema`, `version`, `status`, `deleted`, `created_by`)
VALUES
    (
        'tpl-in-app-plain-message',
        'IN_APP_PLAIN_MESSAGE',
        'system',
        'Default In-App Plain Message',
        'in_app',
        JSON_OBJECT('title', '{{ title | default:"System Message" }}', 'body', '{{ body }}'),
        JSON_OBJECT('title', 'string', 'body', 'string', 'link', 'string'),
        1,
        'active',
        0,
        'system'
    );

INSERT IGNORE INTO `template_version`
    (`id`, `template_id`, `version`, `template_code`, `business_code`, `name`, `channel`, `content`, `params_schema`, `status`, `change_reason`, `created_by`)
VALUES
    (
        'tplv-in-app-plain-message-v1',
        'tpl-in-app-plain-message',
        1,
        'IN_APP_PLAIN_MESSAGE',
        'system',
        'Default In-App Plain Message',
        'in_app',
        JSON_OBJECT('title', '{{ title | default:"System Message" }}', 'body', '{{ body }}'),
        JSON_OBJECT('title', 'string', 'body', 'string', 'link', 'string'),
        'active',
        'seeded template',
        'system'
    );
