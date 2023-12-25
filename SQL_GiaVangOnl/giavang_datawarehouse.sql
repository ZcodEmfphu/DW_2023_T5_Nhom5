/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100432
 Source Host           : localhost:3306
 Source Schema         : giavang_datawarehouse

 Target Server Type    : MySQL
 Target Server Version : 100432
 File Encoding         : 65001

 Date: 25/12/2023 21:28:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for date_dim
-- ----------------------------
DROP TABLE IF EXISTS `date_dim`;
CREATE TABLE `date_dim`  (
  `d_id` int NOT NULL,
  `full_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`d_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product_dim
-- ----------------------------
DROP TABLE IF EXISTS `product_dim`;
CREATE TABLE `product_dim`  (
  `id` int NULL DEFAULT NULL,
  `p_id` int NOT NULL,
  `Name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`p_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product_fact
-- ----------------------------
DROP TABLE IF EXISTS `product_fact`;
CREATE TABLE `product_fact`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Date_ef` int NULL DEFAULT NULL,
  `Date_ex` int NULL DEFAULT NULL,
  `Product_Id` int NULL DEFAULT NULL,
  `BuyingPrice` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `SellingPrice` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `Time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  PRIMARY KEY (`Id`) USING BTREE,
  INDEX `Date_ef`(`Date_ef`) USING BTREE,
  INDEX `Product_Id`(`Product_Id`) USING BTREE,
  CONSTRAINT `product_fact_ibfk_1` FOREIGN KEY (`Date_ef`) REFERENCES `date_dim` (`d_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `product_fact_ibfk_2` FOREIGN KEY (`Product_Id`) REFERENCES `product_dim` (`p_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 273 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
