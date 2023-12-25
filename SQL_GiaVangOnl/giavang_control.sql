/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100432
 Source Host           : localhost:3306
 Source Schema         : giavang_control

 Target Server Type    : MySQL
 Target Server Version : 100432
 File Encoding         : 65001

 Date: 25/12/2023 21:28:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Process` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Username` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Port` int NULL DEFAULT NULL,
  `Title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `FileName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Dest` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Status` int NULL DEFAULT NULL,
  `Flag` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`Id`) USING BTREE,
  INDEX `config_fk`(`Status`) USING BTREE,
  CONSTRAINT `config_fk` FOREIGN KEY (`Status`) REFERENCES `status` (`Id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config
-- ----------------------------
INSERT INTO `config` VALUES (1, 'Loading', 'GiaVang_Staging', 'root', '', 3306, 'Loading dữ liệu từ Staging sang Data Warehouse', NULL, 'GiaVang_DataWarehouse', 7, NULL);
INSERT INTO `config` VALUES (2, NULL, 'GiaVang_DataWarehouse', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);
INSERT INTO `config` VALUES (3, NULL, 'GiaVang_DataMart', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);
INSERT INTO `config` VALUES (4, NULL, 'GiaVang_Control', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);
INSERT INTO `config` VALUES (5, 'GiaVang_Staging', 'localhost', 'root', '', 3306, 'New Title', NULL, 'DataWarehouse', 10, NULL);
INSERT INTO `config` VALUES (6, 'GiaVang_DataWarehouse', 'localhost', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);
INSERT INTO `config` VALUES (7, 'GiaVang_DataMart', 'localhost', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);
INSERT INTO `config` VALUES (8, 'GiaVang_Control', 'localhost', 'root', '', 3306, NULL, NULL, NULL, 1, NULL);

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Time` datetime NULL DEFAULT NULL,
  `Title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Config_Id` int NULL DEFAULT NULL,
  `Status` int NULL DEFAULT NULL,
  PRIMARY KEY (`Id`) USING BTREE,
  INDEX `Config_Id`(`Config_Id`) USING BTREE,
  INDEX `Status`(`Status`) USING BTREE,
  CONSTRAINT `log_ibfk_1` FOREIGN KEY (`Config_Id`) REFERENCES `config` (`Id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `log_ibfk_2` FOREIGN KEY (`Status`) REFERENCES `status` (`Id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of log
-- ----------------------------
INSERT INTO `log` VALUES (1, '2023-12-21 09:29:00', 'Crawling', 'Lấy dữ liệu từ nguồn về file', NULL, 2);
INSERT INTO `log` VALUES (2, '2023-12-21 09:29:19', 'Loading', 'Data Loading TEMP to Product_fact ', 1, 7);
INSERT INTO `log` VALUES (3, '2023-12-21 09:29:49', 'Tranform', 'Dữ liệu của Product_dim đã được cập nhật', 1, 6);
INSERT INTO `log` VALUES (4, '2023-12-21 09:29:49', 'Loaded', 'Dữ liệu được tải từ TEMP vào Product_fact thành công.', 1, 8);

-- ----------------------------
-- Table structure for status
-- ----------------------------
DROP TABLE IF EXISTS `status`;
CREATE TABLE `status`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of status
-- ----------------------------
INSERT INTO `status` VALUES (1, 'Prepared', 'Sẵn sàng lấy dữ liệu');
INSERT INTO `status` VALUES (2, 'Crawling', 'Dữ liệu đang được lấy');
INSERT INTO `status` VALUES (3, 'Crawled', 'Dữ liệu đã được lấy');
INSERT INTO `status` VALUES (4, 'Extracting', 'Dữ liệu đang được truyền');
INSERT INTO `status` VALUES (5, 'Extracted', 'Dữ liệu đã được truyền');
INSERT INTO `status` VALUES (6, 'Transform', 'Hoàn tác thay thế');
INSERT INTO `status` VALUES (7, 'Loading', 'Đang Load dữ liệu');
INSERT INTO `status` VALUES (8, 'Loaded', 'Đã Load dữ liệu');
INSERT INTO `status` VALUES (9, 'Replace', 'Dữ liệu lỗi đang được thay thế');
INSERT INTO `status` VALUES (10, 'Error', 'Lỗi');

SET FOREIGN_KEY_CHECKS = 1;
