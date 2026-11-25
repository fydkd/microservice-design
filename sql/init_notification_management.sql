-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: notification_management
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
-- Current Database: `notification_management`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `notification_management` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `notification_management`;

--
-- Table structure for table `carrier`
--

DROP TABLE IF EXISTS `carrier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrier` (
  `id` varchar(64) NOT NULL,
  `channel_name` varchar(50) NOT NULL,
  `channel_type` varchar(20) NOT NULL,
  `channel_provider` varchar(30) NOT NULL,
  `config_json` json NOT NULL,
  `priority` tinyint NOT NULL DEFAULT '1',
  `weight` int NOT NULL DEFAULT '100',
  `max_retry` tinyint NOT NULL DEFAULT '3',
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'active',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_carrier_channel_provider` (`channel_type`,`channel_provider`),
  KEY `idx_carrier_status` (`channel_type`,`status`,`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrier`
--

LOCK TABLES `carrier` WRITE;
/*!40000 ALTER TABLE `carrier` DISABLE KEYS */;
INSERT INTO `carrier` VALUES ('carrier-email-spring','Spring Mail','email','spring_mail','{}',1,100,3,'Default spring mail carrier','active','2026-06-16 20:41:36','2026-06-16 20:41:36'),('carrier-in-app-system','System In-App','in_app','system','{}',1,100,3,'Default in-app carrier','active','2026-06-16 20:41:36','2026-06-16 20:41:36'),('carrier-push-mock','Mock Push','push','mock_push','{}',1,100,3,'Mock push carrier','disabled','2026-06-16 20:41:36','2026-06-16 20:41:36'),('carrier-sms-mock','Mock SMS','sms','mock_sms','{}',1,100,3,'Mock sms carrier','disabled','2026-06-16 20:41:36','2026-06-16 20:41:36');
/*!40000 ALTER TABLE `carrier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` varchar(64) NOT NULL,
  `task_id` varchar(64) DEFAULT NULL,
  `batch_id` varchar(64) DEFAULT NULL,
  `request_id` varchar(64) DEFAULT NULL,
  `policy_id` varchar(64) DEFAULT NULL,
  `template_id` varchar(64) NOT NULL,
  `template_code` varchar(50) NOT NULL,
  `carrier_id` varchar(64) NOT NULL,
  `channel_code` varchar(20) NOT NULL,
  `provider` varchar(30) DEFAULT NULL,
  `business_type` varchar(50) NOT NULL,
  `sender_uid` varchar(64) NOT NULL DEFAULT 'system',
  `sender_contact` varchar(100) DEFAULT NULL,
  `receiver_uid` varchar(64) NOT NULL DEFAULT '',
  `receiver_contact` varchar(100) NOT NULL,
  `recipient_type` varchar(32) NOT NULL,
  `recipient_value` varchar(255) NOT NULL,
  `params` json DEFAULT NULL,
  `content_title` varchar(255) DEFAULT NULL,
  `content_body` text NOT NULL,
  `template_snapshot` text,
  `send_time_planned` datetime NOT NULL,
  `send_time` datetime DEFAULT NULL,
  `read_time` datetime DEFAULT NULL,
  `response` text,
  `failure_reason` varchar(500) DEFAULT NULL,
  `retry_count` tinyint DEFAULT '0',
  `max_retry` tinyint DEFAULT '3',
  `next_retry_time` datetime DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'pending',
  `created_by` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `canceled` tinyint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_message_task` (`task_id`),
  KEY `idx_message_batch` (`batch_id`),
  KEY `idx_message_request` (`request_id`),
  KEY `idx_message_status_time` (`status`,`send_time_planned`),
  KEY `idx_message_business` (`business_type`,`channel_code`),
  KEY `idx_message_recipient` (`recipient_type`,`recipient_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES ('011062c9dcc14fe4b0af4ef4d3935633','task-011062c9dcc14fe4b0af4ef4d3935633','cec6a3c746c448cea58b475dfafc1148',NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','system','order_notice','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"order_id\": \"SO-BATCH-001\", \"user_name\": \"syl\"}','???? SO-BATCH-001','?? syl,?? SO-BATCH-001 ????','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:39:39','2026-05-10 16:39:39',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 16:39:39','2026-05-10 16:39:39',0),('2d9345236f4c478aa50ba24533def238','task-2d9345236f4c478aa50ba24533def238',NULL,NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','system','order_notice','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"order_id\": \"SO-20260510-001\", \"user_name\": \"syl\"}','???? SO-20260510-001','?? syl,?? SO-20260510-001 ????','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:36:08','2026-05-10 16:36:08',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 16:36:08','2026-05-10 16:36:08',0),('31f9ca0e692e4729947cdf86ea0e4e8b','task-31f9ca0e692e4729947cdf86ea0e4e8b',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"link\": \"http://127.0.0.1:5500\"}','??????','????????????????????','{\"body\": \"????????????????????\", \"title\": \"??????\"}','2026-04-18 16:44:39','2026-04-18 16:44:39','2026-04-18 16:45:11','{\"status\": \"sent\", \"channel\": \"in_app\"}',NULL,0,3,NULL,NULL,'sent','system','2026-04-18 16:44:39','2026-04-18 16:44:39',0),('42949cd3691d45cc81bf4c71eadaee0a','task-42949cd3691d45cc81bf4c71eadaee0a',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"control\", \"title\": \"Control\"}','Control','control','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:14:57','2026-05-10 17:14:57',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:14:57','2026-05-10 17:14:57',0),('61060fc59486409a86cac07fc86723c5','task-61060fc59486409a86cac07fc86723c5',NULL,NULL,NULL,'a07defe4c5904429a9689fc022951008','SMS_NOTICE_2','carrier-sms-mock','sms','mock_sms','sms_notice','system',NULL,'','13800138000','phone','13800138000','{\"code\": \"9527\"}','SMS 9527','Code 9527','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','2026-05-10 17:19:27','2026-05-10 17:19:27',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:19:27','2026-05-10 17:19:27',0),('6a2364f2d3474763936e237a59b0f0e5','task-6a2364f2d3474763936e237a59b0f0e5',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','compat','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"Check link persistence\", \"link\": \"http://example.com/detail/123\", \"title\": \"LinkCheck\"}','LinkCheck','Check link persistence','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:03:16','2026-05-10 17:03:16',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:03:16','2026-05-10 17:03:16',0),('6ba9a76d263a4f36839e7262cbb49bd0','task-6ba9a76d263a4f36839e7262cbb49bd0','f20946e097eb49d280a7b9d201ec0d91','policy-3d84ccea713643b8b0632fbac5f15abb-1778406357662',NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','policy_smoke','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','null','System Message','','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:45:58','2026-05-10 17:45:57',NULL,'{\"status\":\"sent\",\"channel\":\"in_app\"}',NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:45:57','2026-05-10 17:45:57',0),('6e1fa0b7e5fc46ba98f76dcadfae035d','task-6e1fa0b7e5fc46ba98f76dcadfae035d',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"Compatibility check\", \"link\": \"\", \"title\": \"System Notice\"}','System Notice','Compatibility check','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 16:36:36','2026-05-10 16:36:35',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 16:36:35','2026-05-10 16:36:35',0),('7bf30e8c7062412b82c1825ae3a88e4c','task-7bf30e8c7062412b82c1825ae3a88e4c',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system','580b63f2d1d64d8cb3e2a2a59d7f9603',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"link\": \"http://127.0.0.1:5500\"}','????????','???? topbiz ????????????','{\"body\": \"???? topbiz ????????????\", \"title\": \"????????\"}','2026-04-18 16:47:50','2026-04-18 16:47:50','2026-04-18 16:48:13','{\"status\": \"sent\", \"channel\": \"in_app\"}',NULL,0,3,NULL,NULL,'sent','580b63f2d1d64d8cb3e2a2a59d7f9603','2026-04-18 16:47:50','2026-04-18 16:47:50',0),('81e3d9e6444f43a9a546d471a7638a46','task-81e3d9e6444f43a9a546d471a7638a46',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','health_check','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"From smoke test\", \"title\": \"Health\"}','Health','From smoke test','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:01:28','2026-05-10 17:01:28',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:01:28','2026-05-10 17:01:28',0),('916ec8dc97f24f9981a2dfc18f2f5dee','task-916ec8dc97f24f9981a2dfc18f2f5dee',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','compat','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"Check link persistence 3\", \"link\": \"http://example.com/detail/789\", \"title\": \"LinkCheck3\"}','LinkCheck3','Check link persistence 3','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:19:27','2026-05-10 17:19:27',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:19:27','2026-05-10 17:19:27',0),('a9882e13c90a4bbfa5745e023346ade3','task-a9882e13c90a4bbfa5745e023346ade3',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-sms-mock','sms','mock_sms','sms_check','system',NULL,'','13800138000','phone','13800138000','{\"body\": \"SMS channel check\", \"title\": \"SMS\"}','SMS','SMS channel check','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:03:53','2026-05-10 17:13:55',NULL,NULL,NULL,2,3,NULL,NULL,'sent','system','2026-05-10 17:03:52','2026-05-10 17:13:55',0),('ab2bbbbd63b0415d829ac6024104f8a5','task-ab2bbbbd63b0415d829ac6024104f8a5',NULL,NULL,NULL,'e8fa8672674841cba2ed027430ffa519','SMS_NOTICE','carrier-sms-mock','sms','mock_sms','sms_notice','system',NULL,'','13800138000','phone','13800138000','{\"code\": \"9527\"}','SMS 9527','Code 9527','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','2026-05-10 17:14:57','2026-05-10 17:14:57',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:14:57','2026-05-10 17:14:57',0),('b21755fb187d43a1981dad124fd7ca3c','task-b21755fb187d43a1981dad124fd7ca3c',NULL,NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','system','order_notice','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"order_id\": \"SO-DRAFT-CHECK\", \"user_name\": \"syl\"}','???? SO-DRAFT-CHECK','?? syl,?? SO-DRAFT-CHECK ????','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:39:39','2026-05-10 16:39:39',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 16:39:39','2026-05-10 16:39:39',0),('caf8eaa27da64e908b13694cb394f27d','task-caf8eaa27da64e908b13694cb394f27d',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','compat','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"body\": \"Check link persistence 2\", \"link\": \"http://example.com/detail/456\", \"title\": \"LinkCheck2\"}','LinkCheck2','Check link persistence 2','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:14:13','2026-05-10 17:14:12',NULL,NULL,NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:14:12','2026-05-10 17:14:12',0),('d5067b6572ec4b0e817ea9e93d29e8b5','task-d5067b6572ec4b0e817ea9e93d29e8b5',NULL,NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','security','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','{\"link\": \"\"}','????','????????','{\"body\": \"????????\", \"title\": \"????\"}','2026-04-18 16:45:36','2026-04-18 16:45:36','2026-04-18 16:45:36','{\"status\": \"sent\", \"channel\": \"in_app\"}',NULL,0,3,NULL,NULL,'sent','system','2026-04-18 16:45:36','2026-04-18 16:45:36',0),('fdf872a6c7d04ef0a2f34dc9392faf93','task-fdf872a6c7d04ef0a2f34dc9392faf93','9c13f30879524013ae6d08c0516adbad','policy-7cf528bbedc14b9b9101bc6ced7d3f53-1778406702548','7cf528bbedc14b9b9101bc6ced7d3f53','tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','policy_smoke_2','system',NULL,'580b63f2d1d64d8cb3e2a2a59d7f9603','580b63f2d1d64d8cb3e2a2a59d7f9603','user_id','580b63f2d1d64d8cb3e2a2a59d7f9603','null','System Message','','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:51:43','2026-05-10 17:51:42',NULL,'{\"status\":\"sent\",\"channel\":\"in_app\"}',NULL,0,3,NULL,NULL,'sent','system','2026-05-10 17:51:42','2026-05-10 17:51:42',0);
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `policy`
--

DROP TABLE IF EXISTS `policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy` (
  `id` varchar(64) NOT NULL,
  `policy_name` varchar(100) NOT NULL,
  `business_code` varchar(50) NOT NULL,
  `task_id` varchar(64) DEFAULT NULL,
  `template_id` varchar(64) DEFAULT NULL,
  `template_code` varchar(50) DEFAULT NULL,
  `carrier_id` varchar(64) DEFAULT NULL,
  `channel_type` varchar(20) DEFAULT NULL,
  `dynamic_condition` text,
  `send_mode` varchar(20) NOT NULL,
  `send_time` datetime DEFAULT NULL,
  `recurring_rule` varchar(100) DEFAULT NULL,
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `recurring_end_time` datetime DEFAULT NULL,
  `max_executions` int DEFAULT NULL,
  `last_execute_time` datetime DEFAULT NULL,
  `next_execute_time` datetime DEFAULT NULL,
  `last_fire_time` datetime DEFAULT NULL,
  `next_fire_time` datetime DEFAULT NULL,
  `priority` tinyint DEFAULT '5',
  `max_retry` tinyint DEFAULT '3',
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'active',
  `created_by` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_policy_business` (`business_code`,`status`),
  KEY `idx_policy_task` (`task_id`),
  KEY `idx_policy_schedule` (`status`,`next_execute_time`,`next_fire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `policy`
--

LOCK TABLES `policy` WRITE;
/*!40000 ALTER TABLE `policy` DISABLE KEYS */;
INSERT INTO `policy` VALUES ('3d84ccea713643b8b0632fbac5f15abb','Immediate Policy Smoke','policy_smoke',NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE',NULL,'in_app','{\"api\":\"http://127.0.0.1:5800/tmp_policy_recipients.json\"}','immediate',NULL,NULL,'2026-05-10 17:45:36',NULL,NULL,NULL,'2026-05-10 17:45:57',NULL,'2026-05-10 17:45:57',NULL,5,3,NULL,'active','system','2026-05-10 17:45:36','2026-05-10 17:45:57'),('621bd1367e3f4b31a71147b394db38d2','Policy Update Check','homework_unfinished',NULL,NULL,NULL,NULL,NULL,'{\"api\":\"/api/homework/unfinished_students\"}','immediate',NULL,NULL,'2026-05-10 17:24:55',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,3,'updated','disabled','system','2026-05-10 17:24:55','2026-05-10 17:24:55'),('7cf528bbedc14b9b9101bc6ced7d3f53','Immediate Policy Smoke 2','policy_smoke_2',NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE',NULL,'in_app','{\"api\":\"http://127.0.0.1:5800/tmp_policy_recipients.json\"}','immediate',NULL,NULL,'2026-05-10 17:51:31',NULL,NULL,NULL,'2026-05-10 17:51:42',NULL,'2026-05-10 17:51:42',NULL,5,3,NULL,'active','system','2026-05-10 17:51:31','2026-05-10 17:51:42');
/*!40000 ALTER TABLE `policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` varchar(64) NOT NULL,
  `policy_id` varchar(64) DEFAULT NULL,
  `batch_id` varchar(64) DEFAULT NULL,
  `template_id` varchar(64) NOT NULL,
  `template_code` varchar(50) NOT NULL,
  `carrier_id` varchar(64) NOT NULL,
  `channel_code` varchar(20) NOT NULL,
  `business_type` varchar(50) NOT NULL,
  `sender_uid` varchar(64) NOT NULL DEFAULT 'system',
  `url` varchar(100) DEFAULT NULL,
  `receiverlist` json NOT NULL,
  `params` json DEFAULT NULL,
  `template_snapshot` text,
  `send_time_planned` datetime NOT NULL,
  `max_retry` tinyint DEFAULT '3',
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'waiting',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_policy` (`policy_id`),
  KEY `idx_task_status_time` (`status`,`send_time_planned`),
  KEY `idx_task_business` (`business_type`,`channel_code`),
  KEY `idx_task_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES ('task-011062c9dcc14fe4b0af4ef4d3935633',NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','order_notice','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"order_id\": \"SO-BATCH-001\", \"user_name\": \"syl\"}','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:39:39',3,NULL,'completed','2026-05-10 16:39:39','2026-05-10 16:39:39'),('task-2d9345236f4c478aa50ba24533def238',NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','order_notice','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"order_id\": \"SO-20260510-001\", \"user_name\": \"syl\"}','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:36:08',3,NULL,'completed','2026-05-10 16:36:08','2026-05-10 16:36:08'),('task-31f9ca0e692e4729947cdf86ea0e4e8b',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"link\": \"http://127.0.0.1:5500\"}','{\"body\": \"????????????????????\", \"title\": \"??????\"}','2026-04-18 16:44:39',3,NULL,'completed','2026-04-18 16:44:39','2026-04-18 16:44:39'),('task-42949cd3691d45cc81bf4c71eadaee0a',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"control\", \"title\": \"Control\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:14:57',3,NULL,'completed','2026-05-10 17:14:57','2026-05-10 17:14:57'),('task-61060fc59486409a86cac07fc86723c5',NULL,NULL,'a07defe4c5904429a9689fc022951008','SMS_NOTICE_2','carrier-sms-mock','sms','sms_notice','system',NULL,'[{\"type\": \"phone\", \"value\": \"13800138000\"}]','{\"code\": \"9527\"}','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','2026-05-10 17:19:27',3,NULL,'completed','2026-05-10 17:19:27','2026-05-10 17:19:27'),('task-6a2364f2d3474763936e237a59b0f0e5',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','compat','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"Check link persistence\", \"link\": \"http://example.com/detail/123\", \"title\": \"LinkCheck\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:03:16',3,NULL,'completed','2026-05-10 17:03:16','2026-05-10 17:03:16'),('task-6ba9a76d263a4f36839e7262cbb49bd0',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','policy_smoke','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','null','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:45:58',3,NULL,'completed','2026-05-10 17:45:57','2026-05-10 17:45:57'),('task-6e1fa0b7e5fc46ba98f76dcadfae035d',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"Compatibility check\", \"link\": \"\", \"title\": \"System Notice\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 16:36:36',3,NULL,'completed','2026-05-10 16:36:35','2026-05-10 16:36:35'),('task-7bf30e8c7062412b82c1825ae3a88e4c',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','system','580b63f2d1d64d8cb3e2a2a59d7f9603',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"link\": \"http://127.0.0.1:5500\"}','{\"body\": \"???? topbiz ????????????\", \"title\": \"????????\"}','2026-04-18 16:47:50',3,NULL,'completed','2026-04-18 16:47:50','2026-04-18 16:47:50'),('task-81e3d9e6444f43a9a546d471a7638a46',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','health_check','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"From smoke test\", \"title\": \"Health\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:01:28',3,NULL,'completed','2026-05-10 17:01:28','2026-05-10 17:01:28'),('task-916ec8dc97f24f9981a2dfc18f2f5dee',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','compat','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"Check link persistence 3\", \"link\": \"http://example.com/detail/789\", \"title\": \"LinkCheck3\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:19:27',3,NULL,'completed','2026-05-10 17:19:27','2026-05-10 17:19:27'),('task-a9882e13c90a4bbfa5745e023346ade3',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-sms-mock','sms','sms_check','system',NULL,'[{\"type\": \"phone\", \"value\": \"13800138000\"}]','{\"body\": \"SMS channel check\", \"title\": \"SMS\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:03:53',3,NULL,'completed','2026-05-10 17:03:52','2026-05-10 17:13:55'),('task-ab2bbbbd63b0415d829ac6024104f8a5',NULL,NULL,'e8fa8672674841cba2ed027430ffa519','SMS_NOTICE','carrier-sms-mock','sms','sms_notice','system',NULL,'[{\"type\": \"phone\", \"value\": \"13800138000\"}]','{\"code\": \"9527\"}','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','2026-05-10 17:14:57',3,NULL,'completed','2026-05-10 17:14:57','2026-05-10 17:14:57'),('task-b21755fb187d43a1981dad124fd7ca3c',NULL,NULL,'e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','carrier-in-app-system','in_app','order_notice','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"order_id\": \"SO-DRAFT-CHECK\", \"user_name\": \"syl\"}','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','2026-05-10 16:39:39',3,NULL,'completed','2026-05-10 16:39:39','2026-05-10 16:39:39'),('task-caf8eaa27da64e908b13694cb394f27d',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','compat','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"body\": \"Check link persistence 2\", \"link\": \"http://example.com/detail/456\", \"title\": \"LinkCheck2\"}','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:14:13',3,NULL,'completed','2026-05-10 17:14:12','2026-05-10 17:14:12'),('task-d5067b6572ec4b0e817ea9e93d29e8b5',NULL,NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','security','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','{\"link\": \"\"}','{\"body\": \"????????\", \"title\": \"????\"}','2026-04-18 16:45:36',3,NULL,'completed','2026-04-18 16:45:36','2026-04-18 16:45:36'),('task-fdf872a6c7d04ef0a2f34dc9392faf93','7cf528bbedc14b9b9101bc6ced7d3f53',NULL,'tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','carrier-in-app-system','in_app','policy_smoke_2','system',NULL,'[{\"type\": \"user_id\", \"value\": \"580b63f2d1d64d8cb3e2a2a59d7f9603\"}]','null','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','2026-05-10 17:51:43',3,NULL,'completed','2026-05-10 17:51:42','2026-05-10 17:51:42');
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template`
--

DROP TABLE IF EXISTS `template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template` (
  `id` varchar(64) NOT NULL,
  `template_code` varchar(50) NOT NULL,
  `template_name` varchar(50) NOT NULL,
  `carrier_id` varchar(64) DEFAULT NULL,
  `channel_code` varchar(20) NOT NULL,
  `provider_template_id` varchar(100) DEFAULT NULL,
  `business_code` varchar(50) NOT NULL,
  `content` json NOT NULL,
  `variable_definition` json DEFAULT NULL,
  `version` varchar(32) NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'draft',
  `created_by` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_template_business` (`business_code`,`channel_code`,`status`),
  KEY `idx_template_carrier` (`carrier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template`
--

LOCK TABLES `template` WRITE;
/*!40000 ALTER TABLE `template` DISABLE KEYS */;
INSERT INTO `template` VALUES ('18cfe854a50d4d3480960c944325b50d','DRAFT_ONLY_CHECK','Draft Only Check',NULL,'in_app',NULL,'draft_only','{\"body\": \"Draft body {{ order_id }}\", \"title\": \"Draft {{ order_id }}\"}','null','1',NULL,'draft','system','2026-05-10 16:40:23','2026-05-10 16:40:23'),('a07defe4c5904429a9689fc022951008','SMS_NOTICE_2','Sms Notice 2',NULL,'sms',NULL,'sms_notice','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','null','1',NULL,'active','system','2026-05-10 17:19:27','2026-05-10 17:19:27'),('e1b8cad4f68d483a85572159f8c13783','ORDER_NOTICE_IN_APP','Order Notice In App',NULL,'in_app',NULL,'order_notice','{\"body\": \"?? {{ user_name | default:\\\"??\\\" }},?? {{ order_id }} ????\", \"title\": \"???? {{ order_id }}\"}','{\"order_id\": \"string\", \"user_name\": \"string\"}','1',NULL,'active','system','2026-05-10 16:35:33','2026-05-10 16:39:38'),('e8fa8672674841cba2ed027430ffa519','SMS_NOTICE','Sms Notice',NULL,'sms',NULL,'sms_notice','{\"body\": \"Code {{ code }}\", \"title\": \"SMS {{ code }}\"}','null','1',NULL,'active','system','2026-05-10 17:14:57','2026-05-10 17:14:57'),('tpl-in-app-plain-message','IN_APP_PLAIN_MESSAGE','Default In-App Plain Message',NULL,'in_app',NULL,'system','{\"body\": \"{{ body }}\", \"title\": \"{{ title | default:\\\"System Message\\\" }}\"}','{\"body\": \"string\", \"link\": \"string\", \"title\": \"string\"}','1',NULL,'active','system','2026-05-10 16:32:30','2026-05-10 16:32:30');
/*!40000 ALTER TABLE `template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'notification_management'
--

--
-- Dumping routines for database 'notification_management'
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
