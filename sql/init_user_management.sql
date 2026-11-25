-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: user_management
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `user_management`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `user_management` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `user_management`;

--
-- Table structure for table `attribute_data`
--

DROP TABLE IF EXISTS `attribute_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attribute_data` (
  `id` varchar(64) NOT NULL COMMENT '扩展属性记录ID',
  `name` varchar(100) NOT NULL COMMENT '属性名称',
  `description` tinytext COMMENT '描述',
  `extra_data` json DEFAULT NULL COMMENT '属性值/属性配置',
  `group_id` varchar(64) DEFAULT NULL COMMENT '关联组织ID',
  `user_id` varchar(64) DEFAULT NULL COMMENT '关联用户ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_attribute_group_id` (`group_id`),
  KEY `idx_attribute_user_id` (`user_id`),
  CONSTRAINT `fk_attribute_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
  CONSTRAINT `fk_attribute_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属性扩展表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attribute_data`
--

LOCK TABLES `attribute_data` WRITE;
/*!40000 ALTER TABLE `attribute_data` DISABLE KEYS */;
INSERT INTO `attribute_data` VALUES ('7afdb166c85f479697c1bbe90e40b519','学号',NULL,'{\"sort\": 0, \"value\": \"2024091203007\", \"options\": [], \"fieldKey\": \"studentid\", \"required\": false, \"fieldType\": \"textarea\", \"defaultValue\": null}',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','2026-04-18 07:17:35','2026-04-18 07:17:35'),('8f335a5939b54c538a61621374e75bd7','学号',NULL,'{\"sort\": 0, \"value\": null, \"options\": [], \"fieldKey\": \"studentid\", \"required\": false, \"fieldType\": \"textarea\", \"defaultValue\": null}','group-admin',NULL,'2026-04-18 07:10:33','2026-04-18 07:10:33');
/*!40000 ALTER TABLE `attribute_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group` (
  `id` varchar(64) NOT NULL COMMENT '组织ID',
  `name` varchar(100) NOT NULL COMMENT '组织名称',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父组织ID，补足层级组织能力',
  `type` enum('dept','project','tenant','role_group','custom') NOT NULL DEFAULT 'custom' COMMENT '组织类型',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：1正常，2禁用',
  `description` tinytext COMMENT '描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `extension` varchar(255) DEFAULT NULL COMMENT '兼容ER图保留字段',
  `extra_data` json DEFAULT NULL COMMENT '扩展字段，临时密码/负责人等可放这里',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_name` (`name`),
  KEY `idx_group_parent_id` (`parent_id`),
  KEY `idx_group_status` (`status`),
  KEY `idx_group_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group`
--

LOCK TABLES `group` WRITE;
/*!40000 ALTER TABLE `group` DISABLE KEYS */;
INSERT INTO `group` VALUES ('group-admin','管理员组',NULL,'role_group',1,'系统管理员默认分组','2026-04-17 17:57:01','2026-04-17 18:45:59','system',NULL,'{\"role\": \"admin\", \"builtIn\": true}');
/*!40000 ALTER TABLE `group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login_user`
--

DROP TABLE IF EXISTS `login_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_user` (
  `id` varchar(64) NOT NULL COMMENT '登录日志ID',
  `credential_type` enum('password','sms_code','email_code','oauth','sso') NOT NULL COMMENT '登录凭证类型',
  `login_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `login_ip` varchar(64) DEFAULT NULL COMMENT '登录IP，补足审计能力',
  `login_result` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '登录结果：1成功，2失败',
  PRIMARY KEY (`id`),
  KEY `idx_login_user_user_id` (`user_id`),
  KEY `idx_login_user_time` (`login_time`),
  KEY `idx_login_user_credential_type` (`credential_type`),
  CONSTRAINT `fk_login_user_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录审计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login_user`
--

LOCK TABLES `login_user` WRITE;
/*!40000 ALTER TABLE `login_user` DISABLE KEYS */;
INSERT INTO `login_user` VALUES ('07c74ac7a63443878859897c5904bd36','password','2026-04-18 08:24:19','580b63f2d1d64d8cb3e2a2a59d7f9603','127.0.0.1',2),('0c9db8e8fe5a4121afdbcb552abdb76d','password','2026-04-18 06:34:56','580b63f2d1d64d8cb3e2a2a59d7f9603','127.0.0.1',1),('55f0747292ee4a6ba737c1a1149f64d8','password','2026-04-18 07:17:12','580b63f2d1d64d8cb3e2a2a59d7f9603','127.0.0.1',1),('84fe44feb7a8450cb5641170942fdc19','password','2026-04-18 07:17:12','580b63f2d1d64d8cb3e2a2a59d7f9603','127.0.0.1',1),('d91d7d85016b46c6a6a1eb8394f65e49','password','2026-04-18 06:35:13','580b63f2d1d64d8cb3e2a2a59d7f9603','127.0.0.1',1);
/*!40000 ALTER TABLE `login_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` varchar(64) NOT NULL COMMENT '权限ID',
  `code` varchar(100) NOT NULL COMMENT '权限编码，补足权限唯一标识硬需求',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父权限ID，支持树形菜单',
  `path` varchar(255) DEFAULT NULL COMMENT '前端路由路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：1正常，2停用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `description` tinytext COMMENT '权限描述',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `method_type` enum('menu','api','button','data') NOT NULL DEFAULT 'menu' COMMENT '权限类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`),
  KEY `idx_permission_parent_id` (`parent_id`),
  KEY `idx_permission_method_type` (`method_type`),
  KEY `idx_permission_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES ('perm-api-group-create','api:group:create','组织创建','perm-group-mgmt','/api/v1/groups','api',220,1,'2026-04-17 18:12:56','system','鍒涘缓缁勭粐鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-delete','api:group:delete','组织删除','perm-group-mgmt','/api/v1/groups','api',227,1,'2026-04-17 18:12:56','system','鍒犻櫎缁勭粐鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-detail','api:group:detail','组织详情','perm-group-mgmt','/api/v1/groups/{groupId}','api',224,1,'2026-04-17 18:12:56','system','鏌ヨ?缁勭粐璇︽儏鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-detail-my','api:group:detailMy','我的组织详情','perm-group-mgmt','/api/v1/groups/{groupId}/my','api',225,1,'2026-04-17 18:12:56','system','鏌ヨ?鎴戠殑缁勭粐璇︽儏鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-join','api:group:join','组织加入','perm-group-mgmt','/api/v1/groups/{groupId}/join','api',233,1,'2026-04-17 18:12:56','system','鍔犲叆缁勭粐鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-list','api:group:list','组织查询','perm-group-mgmt','/api/v1/groups','api',221,1,'2026-04-17 18:12:56','system','鏌ヨ?缁勭粐鍒楄〃鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-list-my','api:group:listMy','我的组织查询','perm-group-mgmt','/api/v1/groups/my','api',222,1,'2026-04-17 18:12:56','system','鏌ヨ?鎴戠殑缁勭粐鍒楄〃鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-list-public','api:group:listPublic','公开组织查询','perm-group-mgmt','/api/v1/groups/public','api',223,1,'2026-04-17 18:12:56','system','鏌ヨ?鍏?紑缁勭粐鍒楄〃鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-member-add','api:group:member:add','组织成员添加','perm-group-mgmt','/api/v1/groups/{groupId}/members','api',228,1,'2026-04-17 18:12:56','system','娣诲姞缁勭粐鎴愬憳鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-member-remove','api:group:member:remove','组织成员移除','perm-group-mgmt','/api/v1/groups/{groupId}/members','api',229,1,'2026-04-17 18:12:56','system','绉婚櫎缁勭粐鎴愬憳鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-member-upgrade','api:group:member:upgrade','组织成员升级','perm-group-mgmt','/api/v1/groups/{groupId}/members/{userId}/upgrade','api',230,1,'2026-04-17 18:12:56','system','鍗囩骇缁勭粐鎴愬憳瑙掕壊鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-permission-grant','api:group:permission:grant','组织权限授权','perm-group-mgmt','/api/v1/groups/{groupId}/permissions','api',235,1,'2026-04-17 18:12:56','system','缁勭粐鏉冮檺鎺堟潈鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-permission-list','api:group:permission:list','组织权限查询','perm-group-mgmt','/api/v1/groups/{groupId}/permissions','api',234,1,'2026-04-17 18:12:56','system','鏌ヨ?缁勭粐鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-permission-revoke','api:group:permission:revoke','组织权限回收','perm-group-mgmt','/api/v1/groups/{groupId}/permissions','api',236,1,'2026-04-17 18:12:56','system','缁勭粐鏉冮檺鍥炴敹鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-temp-password-clear','api:group:tempPassword:clear','临时密码清除','perm-group-mgmt','/api/v1/groups/{groupId}/temp-password','api',232,1,'2026-04-17 18:12:56','system','娓呴櫎缁勭粐涓存椂瀵嗙爜鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-temp-password-set','api:group:tempPassword:set','临时密码设置','perm-group-mgmt','/api/v1/groups/{groupId}/temp-password','api',231,1,'2026-04-17 18:12:56','system','璁剧疆缁勭粐涓存椂瀵嗙爜鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-group-update','api:group:update','组织修改','perm-group-mgmt','/api/v1/groups','api',226,1,'2026-04-17 18:12:56','system','淇?敼缁勭粐鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-permission-create','api:permission:create','权限创建','perm-permission-mgmt','/api/v1/permissions','api',320,1,'2026-04-17 18:12:56','system','鍒涘缓鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-permission-delete','api:permission:delete','权限删除','perm-permission-mgmt','/api/v1/permissions/{permId}','api',323,1,'2026-04-17 18:12:56','system','鍒犻櫎鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-permission-group-list','api:permission:group:list','权限下组织查询','perm-permission-mgmt','/api/v1/permissions/{permId}/groups','api',324,1,'2026-04-17 18:12:56','system','鏌ヨ?鏌愭潈闄愬叧鑱旂粍缁囨帴鍙ｆ潈闄','2026-04-17 18:45:59','api'),('perm-api-permission-list','api:permission:list','权限查询','perm-permission-mgmt','/api/v1/permissions','api',321,1,'2026-04-17 18:12:56','system','鏌ヨ?鏉冮檺鍒楄〃鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-permission-update','api:permission:update','权限修改','perm-permission-mgmt','/api/v1/permissions/{permId}','api',322,1,'2026-04-17 18:12:56','system','淇?敼鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-permission-user-list','api:permission:user:list','权限下用户查询','perm-permission-mgmt','/api/v1/permissions/{permId}/users','api',325,1,'2026-04-17 18:12:56','system','鏌ヨ?鏌愭潈闄愬叧鑱旂敤鎴锋帴鍙ｆ潈闄','2026-04-17 18:45:59','api'),('perm-api-user-attribute-create','api:user:attribute:create','用户属性创建','perm-user-mgmt','/api/v1/users/{userId}/attributes','api',130,1,'2026-04-17 18:12:56','system','鍒涘缓鐢ㄦ埛灞炴?鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-attribute-delete','api:user:attribute:delete','用户属性删除','perm-user-mgmt','/api/v1/users/{userId}/attributes/{attributeId}','api',132,1,'2026-04-17 18:12:56','system','鍒犻櫎鐢ㄦ埛灞炴?鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-attribute-list','api:user:attribute:list','用户属性查询','perm-user-mgmt','/api/v1/users/{userId}/attributes','api',129,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛灞炴?鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-attribute-update','api:user:attribute:update','用户属性修改','perm-user-mgmt','/api/v1/users/{userId}/attributes/{attributeId}','api',131,1,'2026-04-17 18:12:56','system','淇?敼鐢ㄦ埛灞炴?鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-batch-delete','api:user:batchDelete','用户批量删除','perm-user-mgmt','/api/v1/users/batch','api',126,1,'2026-04-17 18:12:56','system','鎵归噺鍒犻櫎鐢ㄦ埛鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-change-password','api:user:changePassword','当前用户改密','perm-user-mgmt','/api/v1/users/me/password','api',127,1,'2026-04-17 18:12:56','system','褰撳墠鐢ㄦ埛淇?敼瀵嗙爜鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-create','api:user:create','用户创建','perm-user-mgmt','/api/v1/users','api',120,1,'2026-04-17 18:12:56','system','鍒涘缓鐢ㄦ埛鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-delete','api:user:delete','用户删除','perm-user-mgmt','/api/v1/users/{userId}','api',125,1,'2026-04-17 18:12:56','system','鍒犻櫎鐢ㄦ埛鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-detail','api:user:detail','用户详情','perm-user-mgmt','/api/v1/users/{userId}/detail','api',122,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛璇︽儏鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-list','api:user:list','用户查询','perm-user-mgmt','/api/v1/users','api',121,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛鍒楄〃鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-permission-effective','api:user:permission:effective','用户有效权限查询','perm-permission-mgmt','/api/v1/users/{userId}/permissions/effective','api',327,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛鏈夋晥鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-permission-grant','api:user:permission:grant','用户权限授权','perm-permission-mgmt','/api/v1/users/{userId}/permissions','api',329,1,'2026-04-17 18:12:56','system','鐢ㄦ埛鏉冮檺鎺堟潈鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-permission-list','api:user:permission:list','用户权限查询','perm-permission-mgmt','/api/v1/users/{userId}/permissions','api',326,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛鏉冮檺鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-permission-revoke','api:user:permission:revoke','用户权限回收','perm-permission-mgmt','/api/v1/users/{userId}/permissions','api',330,1,'2026-04-17 18:12:56','system','鐢ㄦ埛鏉冮檺鍥炴敹鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-permission-source','api:user:permission:source','用户权限来源查询','perm-permission-mgmt','/api/v1/users/{userId}/permissions/source/{permId}','api',328,1,'2026-04-17 18:12:56','system','鏌ヨ?鐢ㄦ埛鏉冮檺鏉ユ簮鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-reset-password','api:user:resetPassword','用户重置密码','perm-user-mgmt','/api/v1/users/{userId}/password/reset','api',128,1,'2026-04-17 18:12:56','system','閲嶇疆鐢ㄦ埛瀵嗙爜鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-update','api:user:update','用户修改','perm-user-mgmt','/api/v1/users','api',123,1,'2026-04-17 18:12:56','system','淇?敼鐢ㄦ埛鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-api-user-update-me','api:user:updateMe','当前用户修改','perm-user-mgmt','/api/v1/users/me','api',124,1,'2026-04-17 18:12:56','system','淇?敼褰撳墠鐢ㄦ埛璧勬枡鎺ュ彛鏉冮檺','2026-04-17 18:45:59','api'),('perm-dashboard','menu:dashboard','工作台',NULL,'/dashboard','dashboard',10,1,'2026-04-17 17:57:01','system','鎺у埗鍙伴?椤佃彍鍗','2026-04-17 18:45:59','menu'),('perm-device-sessions','menu:device:sessions','会话设备','perm-system-monitor','/devices','device',52,1,'2026-04-17 17:57:01','system','鏌ョ湅鍜屼笅绾胯?澶囦細璇','2026-04-17 18:45:59','menu'),('perm-flow-register','menu:flow:register','注册流程','perm-system-monitor','/flow/register','flow',51,1,'2026-04-17 17:57:01','system','鏌ョ湅娉ㄥ唽娴佺▼','2026-04-17 18:45:59','menu'),('perm-group-create','menu:group:create','创建组织','perm-group-mgmt','/groups/create','plus',32,1,'2026-04-17 17:57:01','system','鍒涘缓缁勭粐','2026-04-17 18:45:59','menu'),('perm-group-delete','menu:group:delete','删除组织','perm-group-mgmt','/groups/delete','delete',34,1,'2026-04-17 17:57:01','system','鍒犻櫎缁勭粐','2026-04-17 18:45:59','menu'),('perm-group-list','menu:group:list','组织列表','perm-group-mgmt','/groups/list','list',31,1,'2026-04-17 17:57:01','system','鏌ョ湅缁勭粐鍒楄〃','2026-04-17 18:45:59','menu'),('perm-group-member','menu:group:member','成员管理','perm-group-mgmt','/groups/members','users',35,1,'2026-04-17 17:57:01','system','绠＄悊缁勭粐鎴愬憳','2026-04-17 18:45:59','menu'),('perm-group-mgmt','menu:groupManagement','组织管理',NULL,'/groups','team',30,1,'2026-04-17 17:57:01','system','缁勭粐绠＄悊涓昏彍鍗','2026-04-17 18:45:59','menu'),('perm-group-permission','menu:group:permission','组织权限','perm-group-mgmt','/groups/permissions','shield',36,1,'2026-04-17 17:57:01','system','绠＄悊缁勭粐鏉冮檺','2026-04-17 18:45:59','menu'),('perm-group-temp-password','menu:group:tempPassword','临时密码','perm-group-mgmt','/groups/temp-password','key',37,1,'2026-04-17 17:57:01','system','绠＄悊缁勭粐涓存椂瀵嗙爜','2026-04-17 18:45:59','menu'),('perm-group-update','menu:group:update','编辑组织','perm-group-mgmt','/groups/update','edit',33,1,'2026-04-17 17:57:01','system','缂栬緫缁勭粐','2026-04-17 18:45:59','menu'),('perm-permission-create','menu:permission:create','创建权限','perm-permission-mgmt','/permissions/create','plus',42,1,'2026-04-17 17:57:01','system','鍒涘缓鏉冮檺','2026-04-17 18:45:59','menu'),('perm-permission-delete','menu:permission:delete','删除权限','perm-permission-mgmt','/permissions/delete','delete',44,1,'2026-04-17 17:57:01','system','鍒犻櫎鏉冮檺','2026-04-17 18:45:59','menu'),('perm-permission-grant','menu:permission:grant','授权管理','perm-permission-mgmt','/permissions/grant','grant',45,1,'2026-04-17 17:57:01','system','绠＄悊鐢ㄦ埛鍜岀粍缁囨巿鏉','2026-04-17 18:45:59','menu'),('perm-permission-list','menu:permission:list','权限列表','perm-permission-mgmt','/permissions/list','list',41,1,'2026-04-17 17:57:01','system','鏌ョ湅鏉冮檺鍒楄〃','2026-04-17 18:45:59','menu'),('perm-permission-mgmt','menu:permissionManagement','权限管理',NULL,'/permissions','shield',40,1,'2026-04-17 17:57:01','system','鏉冮檺绠＄悊涓昏彍鍗','2026-04-17 18:45:59','menu'),('perm-permission-update','menu:permission:update','编辑权限','perm-permission-mgmt','/permissions/update','edit',43,1,'2026-04-17 17:57:01','system','缂栬緫鏉冮檺','2026-04-17 18:45:59','menu'),('perm-system-monitor','menu:systemMonitor','系统监控与会话设备',NULL,'/flow','monitor',50,1,'2026-04-17 17:57:01','system','绯荤粺鐩戞帶涓庝細璇濊?澶囦富鑿滃崟','2026-04-17 18:45:59','menu'),('perm-user-attribute','menu:user:attribute','用户属性','perm-user-mgmt','/users/attributes','profile',26,1,'2026-04-17 17:57:01','system','缁存姢鐢ㄦ埛灞炴?','2026-04-17 18:45:59','menu'),('perm-user-create','menu:user:create','创建用户','perm-user-mgmt','/users/create','plus',22,1,'2026-04-17 17:57:01','system','鍒涘缓鐢ㄦ埛','2026-04-17 18:45:59','menu'),('perm-user-delete','menu:user:delete','删除用户','perm-user-mgmt','/users/delete','delete',24,1,'2026-04-17 17:57:01','system','鍒犻櫎鐢ㄦ埛','2026-04-17 18:45:59','menu'),('perm-user-list','menu:user:list','用户列表','perm-user-mgmt','/users/list','list',21,1,'2026-04-17 17:57:01','system','鏌ョ湅鐢ㄦ埛鍒楄〃','2026-04-17 18:45:59','menu'),('perm-user-mgmt','menu:userManagement','用户管理',NULL,'/users','user',20,1,'2026-04-17 17:57:01','system','鐢ㄦ埛绠＄悊涓昏彍鍗','2026-04-17 18:45:59','menu'),('perm-user-reset-password','menu:user:resetPassword','重置密码','perm-user-mgmt','/users/reset-password','lock',25,1,'2026-04-17 17:57:01','system','閲嶇疆鐢ㄦ埛瀵嗙爜','2026-04-17 18:45:59','menu'),('perm-user-update','menu:user:update','编辑用户','perm-user-mgmt','/users/update','edit',23,1,'2026-04-17 17:57:01','system','缂栬緫鐢ㄦ埛','2026-04-17 18:45:59','menu');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission_and_group`
--

DROP TABLE IF EXISTS `permission_and_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission_and_group` (
  `group_id` varchar(64) NOT NULL COMMENT '组织ID',
  `permission_id` varchar(64) NOT NULL COMMENT '权限ID',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间，ER图缺失，补足审计',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '授权人ID',
  `description` tinytext COMMENT '备注',
  PRIMARY KEY (`group_id`,`permission_id`),
  KEY `idx_pag_permission_id` (`permission_id`),
  CONSTRAINT `fk_pag_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
  CONSTRAINT `fk_pag_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织权限关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission_and_group`
--

LOCK TABLES `permission_and_group` WRITE;
/*!40000 ALTER TABLE `permission_and_group` DISABLE KEYS */;
INSERT INTO `permission_and_group` VALUES ('group-admin','perm-api-group-create','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-delete','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-detail','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-detail-my','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-join','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-list-my','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-list-public','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-member-add','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-member-remove','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-member-upgrade','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-permission-grant','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-permission-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-permission-revoke','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-temp-password-clear','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-temp-password-set','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-group-update','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-create','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-delete','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-group-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-update','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-permission-user-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-attribute-create','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-attribute-delete','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-attribute-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-attribute-update','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-batch-delete','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-change-password','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-create','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-delete','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-detail','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-permission-effective','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-permission-grant','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-permission-list','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-permission-revoke','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-permission-source','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-reset-password','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-update','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-api-user-update-me','2026-04-17 18:12:56','2026-04-17 18:12:56','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-dashboard','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-device-sessions','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-flow-register','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-create','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-delete','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-list','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-member','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-mgmt','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-permission','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-temp-password','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-group-update','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-create','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-delete','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-grant','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-list','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-mgmt','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-permission-update','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-system-monitor','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-attribute','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-create','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-delete','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-list','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-mgmt','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-reset-password','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺'),('group-admin','perm-user-update','2026-04-17 17:57:01','2026-04-17 17:57:01','system','绠＄悊鍛樼粍榛樿?鎷ユ湁鍏ㄩ儴鏉冮檺');
/*!40000 ALTER TABLE `permission_and_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission_and_user`
--

DROP TABLE IF EXISTS `permission_and_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission_and_user` (
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `permission_id` varchar(64) NOT NULL COMMENT '权限ID',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '授权人ID',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `description` tinytext COMMENT '备注',
  PRIMARY KEY (`user_id`,`permission_id`),
  KEY `idx_pau_permission_id` (`permission_id`),
  CONSTRAINT `fk_pau_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`),
  CONSTRAINT `fk_pau_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户直接权限关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission_and_user`
--

LOCK TABLES `permission_and_user` WRITE;
/*!40000 ALTER TABLE `permission_and_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `permission_and_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` varchar(64) NOT NULL COMMENT '用户ID，兼容UUID/雪花ID字符串',
  `name` varchar(100) NOT NULL COMMENT '用户名/昵称，沿用ER图字段名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号，补足注册/登录硬需求',
  `mail` varchar(128) DEFAULT NULL COMMENT '邮箱，ER图mail长度过短，调整为128',
  `password` varchar(255) NOT NULL COMMENT '密码摘要',
  `salt` varchar(64) DEFAULT NULL COMMENT '密码盐值',
  `gender` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '性别：0未知，1男，2女',
  `description` tinytext COMMENT '备注/描述',
  `extension` longtext COMMENT '兼容ER图的扩展字段',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：1正常，2禁用，3锁定，4已注销',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `last_operator_id` varchar(64) DEFAULT NULL COMMENT '最后操作人ID',
  `extra_data` json DEFAULT NULL COMMENT '结构化扩展字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  UNIQUE KEY `uk_user_mail` (`mail`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_creator_id` (`creator_id`),
  CONSTRAINT `user_chk_1` CHECK (((`phone` is null) or (char_length(`phone`) between 6 and 20)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('580b63f2d1d64d8cb3e2a2a59d7f9603','syl',NULL,'1160579753@qq.com','89cf0fe4c24e9468f8692edc9fa616bd8ae35f33fd0f29136df237773b208662','3ede956fd2a9a61da73cd63681b2f1dc',0,NULL,NULL,1,NULL,'2026-04-17 17:37:31','2026-04-17 17:58:59','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"role\": \"admin\", \"avatar\": \"1160579753@qq.com\", \"remark\": null, \"lockedUntil\": null, \"feishuOpenId\": null, \"feishuUserId\": null, \"feishuUnionId\": null, \"enterpriseEmail\": null, \"failedPasswordAttempts\": 1}');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_and_group`
--

DROP TABLE IF EXISTS `user_and_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_and_group` (
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `group_id` varchar(64) NOT NULL COMMENT '组织ID',
  `extension` longtext COMMENT '关系扩展字段',
  `creator_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `last_operator_id` varchar(64) DEFAULT NULL COMMENT '最后操作人ID',
  `description` tinytext COMMENT '备注',
  `type` varchar(32) DEFAULT 'member' COMMENT '成员类型：admin/member/temporary',
  PRIMARY KEY (`user_id`,`group_id`),
  KEY `idx_uag_group_id` (`group_id`),
  KEY `idx_uag_type` (`type`),
  CONSTRAINT `fk_uag_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
  CONSTRAINT `fk_uag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与组织关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_and_group`
--

LOCK TABLES `user_and_group` WRITE;
/*!40000 ALTER TABLE `user_and_group` DISABLE KEYS */;
INSERT INTO `user_and_group` VALUES ('580b63f2d1d64d8cb3e2a2a59d7f9603','group-admin',NULL,'system','2026-04-17 17:58:59','2026-04-17 17:58:59','system',NULL,'admin');
/*!40000 ALTER TABLE `user_and_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'user_management'
--

--
-- Dumping routines for database 'user_management'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-16 20:45:43
