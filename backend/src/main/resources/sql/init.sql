-- =============================================
-- kuro-ims 智能车间监控系统 数据库初始化脚本
-- =============================================
CREATE DATABASE IF NOT EXISTS kuro_ims DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE kuro_ims;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------- 用户表 ----------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '系统用户表';

-- ---------------- 仓库表 ----------------
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL COMMENT '仓库编码',
    `name`        VARCHAR(64)  NOT NULL COMMENT '仓库名称',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '类型: NORMAL/COLD/HAZARD',
    `location_x`  DOUBLE       NOT NULL DEFAULT 0 COMMENT '3D场景X坐标',
    `location_z`  DOUBLE       NOT NULL DEFAULT 0 COMMENT '3D场景Z坐标',
    `size_x`      DOUBLE       NOT NULL DEFAULT 10 COMMENT '长度(米)',
    `size_z`      DOUBLE       NOT NULL DEFAULT 8 COMMENT '宽度(米)',
    `size_y`      DOUBLE       NOT NULL DEFAULT 6 COMMENT '高度(米)',
    `color`       VARCHAR(16)  DEFAULT '#5B8FF9' COMMENT '展示颜色',
    `manager`     VARCHAR(32)  DEFAULT NULL COMMENT '负责人',
    `capacity`    INT          NOT NULL DEFAULT 100 COMMENT '总容量(托位)',
    `used`        INT          NOT NULL DEFAULT 0 COMMENT '已用容量',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/MAINTAIN/STOP',
    `description` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '仓库表';

-- ---------------- 货柜表 ----------------
DROP TABLE IF EXISTS `wms_container`;
CREATE TABLE `wms_container` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL COMMENT '货柜编码',
    `name`        VARCHAR(64)  NOT NULL COMMENT '货柜名称',
    `type`        VARCHAR(16)  NOT NULL DEFAULT '40HQ' COMMENT '柜型: 20GP/40GP/40HQ/45HQ',
    `length`      DOUBLE       NOT NULL COMMENT '内长(cm)',
    `width`       DOUBLE       NOT NULL COMMENT '内宽(cm)',
    `height`      DOUBLE       NOT NULL COMMENT '内高(cm)',
    `max_weight`  DOUBLE       NOT NULL COMMENT '最大载重(kg)',
    `used_volume` DOUBLE       NOT NULL DEFAULT 0 COMMENT '已用容积(m³)',
    `used_weight` DOUBLE       NOT NULL DEFAULT 0 COMMENT '已用重量(kg)',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'EMPTY' COMMENT 'EMPTY/LOADING/FULL/SHIPPED',
    `warehouse_id` BIGINT      DEFAULT NULL COMMENT '所属仓库',
    `location`    VARCHAR(64)  DEFAULT NULL COMMENT '所在位置',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '货柜表';

-- ---------------- 货物表 ----------------
DROP TABLE IF EXISTS `wms_goods`;
CREATE TABLE `wms_goods` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `code`         VARCHAR(32)  NOT NULL COMMENT '货物编码/SKU',
    `name`         VARCHAR(64)  NOT NULL COMMENT '货物名称',
    `category`     VARCHAR(32)  DEFAULT NULL COMMENT '分类',
    `spec`         VARCHAR(64)  DEFAULT NULL COMMENT '规格',
    `unit`         VARCHAR(16)  NOT NULL DEFAULT '箱' COMMENT '单位',
    `length`       DOUBLE       DEFAULT NULL COMMENT '长(cm)',
    `width`        DOUBLE       DEFAULT NULL COMMENT '宽(cm)',
    `height`       DOUBLE       DEFAULT NULL COMMENT '高(cm)',
    `weight`       DOUBLE       DEFAULT NULL COMMENT '单件重量(kg)',
    `quantity`     INT          NOT NULL DEFAULT 0 COMMENT '数量',
    `batch_no`     VARCHAR(32)  DEFAULT NULL COMMENT '批次号',
    `container_id` BIGINT       DEFAULT NULL COMMENT '所在货柜',
    `warehouse_id` BIGINT       DEFAULT NULL COMMENT '所在仓库',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'IN_STOCK' COMMENT 'IN_STOCK/LOADED/SHIPPED',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_batch` (`code`, `batch_no`)
) ENGINE = InnoDB COMMENT = '货物表';

-- ---------------- 车辆表 ----------------
DROP TABLE IF EXISTS `wms_vehicle`;
CREATE TABLE `wms_vehicle` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `plate_no`     VARCHAR(16) NOT NULL COMMENT '车牌号',
    `driver`       VARCHAR(32) DEFAULT NULL COMMENT '司机',
    `phone`        VARCHAR(16) DEFAULT NULL COMMENT '联系电话',
    `type`         VARCHAR(16) NOT NULL DEFAULT 'TRUCK' COMMENT '类型: TRUCK/VAN/CONTAINER',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'IDLE' COMMENT 'IDLE/LOADING/TRANSIT/UNLOADING',
    `origin`       VARCHAR(64) DEFAULT NULL COMMENT '始发地',
    `destination`  VARCHAR(64) DEFAULT NULL COMMENT '目的地',
    `progress`     INT         NOT NULL DEFAULT 0 COMMENT '运输进度0-100',
    `location_x`   DOUBLE      NOT NULL DEFAULT 0 COMMENT '3D场景X坐标',
    `location_z`   DOUBLE      NOT NULL DEFAULT 0 COMMENT '3D场景Z坐标',
    `container_id` BIGINT      DEFAULT NULL COMMENT '载运货柜',
    `cargo_desc`   VARCHAR(255) DEFAULT NULL COMMENT '货物描述',
    `eta`          DATETIME    DEFAULT NULL COMMENT '预计到达时间',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plate` (`plate_no`)
) ENGINE = InnoDB COMMENT = '运输车辆表';

-- ---------------- 叉车表 ----------------
DROP TABLE IF EXISTS `wms_forklift`;
CREATE TABLE `wms_forklift` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `code`         VARCHAR(32) NOT NULL COMMENT '叉车编号',
    `name`         VARCHAR(64) DEFAULT NULL COMMENT '名称',
    `driver`       VARCHAR(32) DEFAULT NULL COMMENT '操作员',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'IDLE' COMMENT 'IDLE/WORKING/CHARGING/MAINTAIN',
    `battery`      INT         NOT NULL DEFAULT 100 COMMENT '电量0-100',
    `current_task` VARCHAR(64) DEFAULT NULL COMMENT '当前任务',
    `location_x`   DOUBLE      NOT NULL DEFAULT 0,
    `location_z`   DOUBLE      NOT NULL DEFAULT 0,
    `warehouse_id` BIGINT      DEFAULT NULL COMMENT '所属仓库',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '叉车表';

-- ---------------- 任务表 ----------------
DROP TABLE IF EXISTS `wms_task`;
CREATE TABLE `wms_task` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `code`          VARCHAR(32) NOT NULL COMMENT '任务编号',
    `type`          VARCHAR(16) NOT NULL COMMENT 'INBOUND/OUTBOUND/TRANSFER/LOADING',
    `status`        VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DOING/DONE/CANCEL',
    `priority`      INT         NOT NULL DEFAULT 3 COMMENT '1最高5最低',
    `goods_id`      BIGINT      DEFAULT NULL,
    `goods_name`    VARCHAR(64) DEFAULT NULL,
    `quantity`      INT         DEFAULT NULL,
    `from_location` VARCHAR(64) DEFAULT NULL,
    `to_location`   VARCHAR(64) DEFAULT NULL,
    `forklift_id`   BIGINT      DEFAULT NULL,
    `vehicle_id`    BIGINT      DEFAULT NULL,
    `container_id`  BIGINT      DEFAULT NULL,
    `operator`      VARCHAR(32) DEFAULT NULL,
    `remark`        VARCHAR(255) DEFAULT NULL,
    `start_time`    DATETIME    DEFAULT NULL,
    `finish_time`   DATETIME    DEFAULT NULL,
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '作业任务表';

-- ---------------- 车间设备监控表 ----------------
DROP TABLE IF EXISTS `mes_device`;
CREATE TABLE `mes_device` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32) NOT NULL COMMENT '设备编号',
    `name`        VARCHAR(64) NOT NULL COMMENT '设备名称',
    `type`        VARCHAR(32) DEFAULT NULL COMMENT '设备类型',
    `workshop`    VARCHAR(32) DEFAULT NULL COMMENT '所属车间',
    `status`      VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/IDLE/ALARM/STOP',
    `temperature` DOUBLE      DEFAULT NULL COMMENT '温度℃',
    `humidity`    DOUBLE      DEFAULT NULL COMMENT '湿度%',
    `runtime`     DOUBLE      NOT NULL DEFAULT 0 COMMENT '累计运行时长(h)',
    `oee`         DOUBLE      DEFAULT NULL COMMENT '设备综合效率%',
    `location_x`  DOUBLE      NOT NULL DEFAULT 0,
    `location_z`  DOUBLE      NOT NULL DEFAULT 0,
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB COMMENT = '车间设备表';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员: admin / 123456 (BCrypt)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`)
VALUES ('admin', '$2b$10$MFX33K8GegZRXiZpIZKFV.gSXAJk2U7Un5vucnnQb4vvDfdrjpiLy', '系统管理员', 'ADMIN');

-- 仓库（3D 园区坐标）
INSERT INTO `wms_warehouse` (`code`, `name`, `type`, `location_x`, `location_z`, `size_x`, `size_z`, `size_y`, `color`, `manager`, `capacity`, `used`, `status`, `description`) VALUES
('WH-A', 'A区·原材料仓', 'NORMAL', -22, -12, 19, 13, 8.5, '#5B8FF9', '张伟', 500, 326, 'RUNNING', '存放电子元器件与五金原料'),
('WH-B', 'B区·成品仓',   'NORMAL',  22, -12, 21, 13, 9, '#5AD8A6', '李娜', 600, 455, 'RUNNING', '成品整机存储与发货'),
('WH-C', 'C区·冷链仓',   'COLD',    -22,  14, 18, 11, 7.5, '#5D7092', '王强', 300,  88, 'RUNNING', '恒温恒湿精密物料'),
('WH-D', 'D区·周转仓',   'NORMAL',  22,  14, 18, 11, 7.5, '#F6BD16', '赵敏', 400, 271, 'MAINTAIN', '中转暂存与分拣');

-- 货柜
INSERT INTO `wms_container` (`code`, `name`, `type`, `length`, `width`, `height`, `max_weight`, `used_volume`, `used_weight`, `status`, `warehouse_id`, `location`) VALUES
('CTN-001', '40HQ-出口柜01', '40HQ', 1203, 235, 269, 26500, 48.5, 16200, 'LOADING', 2, 'B区月台01'),
('CTN-002', '20GP-内销柜02', '20GP',  589, 235, 239, 21700, 21.2,  9800, 'FULL',    2, 'B区月台02'),
('CTN-003', '40GP-出口柜03', '40GP', 1203, 235, 239, 26500,  0.0,     0, 'EMPTY',   1, 'A区堆场03'),
('CTN-004', '45HQ-出口柜04', '45HQ', 1355, 235, 269, 27600,  0.0,     0, 'EMPTY',   1, 'A区堆场05');

-- 货物
INSERT INTO `wms_goods` (`code`, `name`, `category`, `spec`, `unit`, `length`, `width`, `height`, `weight`, `quantity`, `batch_no`, `container_id`, `warehouse_id`, `status`) VALUES
('SKU-1001', '伺服电机',     '机电',   '750W 220V',  '箱', 60, 40, 40,  18.5, 120, 'B20240401', 1, 2, 'LOADED'),
('SKU-1002', 'PLC控制器',    '电气',   'S7-1200',    '箱', 50, 40, 30,  12.0,  80, 'B20240402', 1, 2, 'LOADED'),
('SKU-1003', '铝合金型材',   '原材料', '4040 欧标',   '捆', 200, 30, 30,  25.0, 200, 'B20240403', NULL, 1, 'IN_STOCK'),
('SKU-1004', '触摸屏一体机', '成品',   '10.1寸',      '箱', 55, 45, 35,  15.0,  60, 'B20240404', NULL, 2, 'IN_STOCK'),
('SKU-1005', '精密轴承',     '原材料', 'SKF 6204',    '箱', 40, 40, 30,   9.5, 150, 'B20240405', NULL, 3, 'IN_STOCK');

-- 车辆
INSERT INTO `wms_vehicle` (`plate_no`, `driver`, `phone`, `type`, `status`, `origin`, `destination`, `progress`, `location_x`, `location_z`, `container_id`, `cargo_desc`, `eta`) VALUES
('粤B·D8832', '刘师傅', '138****2201', 'CONTAINER', 'LOADING',  '东莞工厂', '深圳盐田港', 0,   6, -6, 1, '伺服电机/PLC共200箱', '2024-05-01 18:00:00'),
('粤A·T5517', '陈师傅', '139****3302', 'TRUCK',     'TRANSIT',  '广州仓',   '东莞工厂', 62, -30, 20, NULL, '铝合金型材30捆',   '2024-05-01 16:30:00'),
('粤S·K9012', '黄师傅', '137****4403', 'VAN',       'IDLE',     NULL,       NULL,       0,  12, 18, NULL, NULL, NULL),
('沪C·G3345', '吴师傅', '136****5504', 'CONTAINER', 'TRANSIT',  '上海港',   '东莞工厂', 85,  40, -25, NULL, '精密轴承150箱',    '2024-05-01 15:10:00');

-- 叉车
INSERT INTO `wms_forklift` (`code`, `name`, `driver`, `status`, `battery`, `current_task`, `location_x`, `location_z`, `warehouse_id`) VALUES
('FK-01', '1号电动叉车', '周工', 'WORKING', 76, 'T-20240430-001 装柜作业', 10, -4, 2),
('FK-02', '2号电动叉车', '郑工', 'IDLE',    92, NULL,                    -14, 2, 1),
('FK-03', '3号柴油叉车', '冯工', 'CHARGING', 23, NULL,                    18, 8, 4),
('FK-04', '4号AGV叉车',  NULL,   'WORKING', 64, 'T-20240430-002 入库转运', -6, 12, 3);

-- 任务
INSERT INTO `wms_task` (`code`, `type`, `status`, `priority`, `goods_id`, `goods_name`, `quantity`, `from_location`, `to_location`, `forklift_id`, `vehicle_id`, `container_id`, `operator`, `remark`, `start_time`) VALUES
('T-20240430-001', 'LOADING',  'DOING',   1, 1, '伺服电机',   120, 'B区成品仓', 'CTN-001', 1, 1, 1, '周工', '出口柜优先装', '2024-04-30 09:20:00'),
('T-20240430-002', 'INBOUND',  'DOING',   2, 5, '精密轴承',   150, '收货月台', 'C区冷链仓', 4, NULL, NULL, NULL, '恒温存储', '2024-04-30 10:05:00'),
('T-20240430-003', 'TRANSFER', 'PENDING', 3, 3, '铝合金型材', 30,  'A区原材料仓', 'D区周转仓', NULL, NULL, NULL, NULL, '等待车辆到达', NULL),
('T-20240430-004', 'OUTBOUND', 'PENDING', 2, 4, '触摸屏一体机', 20, 'B区成品仓', '发货月台', NULL, NULL, NULL, NULL, '客户自提', NULL);

-- 车间设备
INSERT INTO `mes_device` (`code`, `name`, `type`, `workshop`, `status`, `temperature`, `humidity`, `runtime`, `oee`, `location_x`, `location_z`) VALUES
('DEV-CNC-01',  'CNC加工中心1#', 'CNC机床',   '一车间', 'RUNNING', 42.5, 45, 1260.5, 86.2, -10, -2),
('DEV-CNC-02',  'CNC加工中心2#', 'CNC机床',   '一车间', 'RUNNING', 45.1, 44, 1180.2, 82.7, -4, -2),
('DEV-ROB-01',  '六轴机器人1#',  '工业机器人', '一车间', 'ALARM',   68.3, 40,  890.0, 61.3,  2, -2),
('DEV-ASM-01',  '自动装配线',    '装配线',    '二车间', 'RUNNING', 36.8, 50, 2100.0, 91.5,  8, -2),
('DEV-INJ-01',  '注塑机3#',      '注塑机',    '二车间', 'IDLE',    28.0, 55,  640.8, 45.0, 14, -2),
('DEV-AGV-01',  'AGV调度系统',   '物流系统',   '全厂',   'RUNNING', 25.0, 48, 3200.0, 95.8,  0,  6);

-- 园区建筑（地图可编辑资产）
DROP TABLE IF EXISTS `wms_building`;
CREATE TABLE `wms_building` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  DEFAULT NULL COMMENT '建筑编号',
    `name`        VARCHAR(64)  NOT NULL COMMENT '建筑名称',
    `model`       VARCHAR(16)  NOT NULL DEFAULT 'WORKSHOP' COMMENT 'OFFICE/WORKSHOP/WAREHOUSE/CANTEEN/QC/VISITOR',
    `pos_x`       DOUBLE       NOT NULL DEFAULT 0 COMMENT '园区X坐标',
    `pos_z`       DOUBLE       NOT NULL DEFAULT 0 COMMENT '园区Z坐标',
    `size_x`      DOUBLE       NOT NULL DEFAULT 20 COMMENT '面宽(X向)',
    `size_y`      DOUBLE       NOT NULL DEFAULT 8  COMMENT '高度',
    `size_z`      DOUBLE       NOT NULL DEFAULT 14 COMMENT '进深(Z向)',
    `manager`     VARCHAR(32)  DEFAULT NULL COMMENT '负责人',
    `capacity`    INT          DEFAULT 0 COMMENT '容量(托位)',
    `used`        INT          DEFAULT 0 COMMENT '已用(托位)',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/MAINTAIN/STOP',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `color`       VARCHAR(16)  DEFAULT NULL COMMENT '屋面颜色',
    `remark`      VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '园区建筑表';

INSERT INTO `wms_building` (`code`, `name`, `model`, `pos_x`, `pos_z`, `size_x`, `size_y`, `size_z`, `manager`, `capacity`, `used`, `status`, `description`) VALUES
('ADM-01', '行政办公楼', 'OFFICE',    -68, -55, 26, 12, 15, '行政部', 0, 0, 'RUNNING', '园区行政办公'),
('WKS-A1', 'A1生产车间', 'WORKSHOP',  -10, -55, 32, 8,  17, '生产部', 0, 0, 'RUNNING', 'A1生产线'),
('WKS-A2', 'A2装配车间', 'WORKSHOP',   52, -55, 32, 8,  17, '生产部', 0, 0, 'RUNNING', 'A2装配线'),
('QC-01',  '质检中心',   'QC',        -68, -8,  22, 7,  14, '质量部', 0, 0, 'RUNNING', '质量检测中心'),
('CAN-01', '员工餐厅',   'CANTEEN',   -20, -8,  19, 6,  13, '后勤部', 0, 0, 'RUNNING', '员工餐厅'),
('VIS-01', '访客中心',   'VISITOR',   -68, 47,  24, 7,  14, '行政部', 0, 0, 'RUNNING', '访客接待中心');

-- 园区作业人员
DROP TABLE IF EXISTS `wms_worker`;
CREATE TABLE `wms_worker` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL COMMENT '工号',
    `name`        VARCHAR(32)  NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `role`        VARCHAR(32)  DEFAULT NULL COMMENT '岗位',
    `department`  VARCHAR(32)  DEFAULT NULL COMMENT '部门',
    `area`        VARCHAR(64)  DEFAULT NULL COMMENT '负责区域',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ON' COMMENT 'ON在职/OFF休假/LEAVE离职',
    `remark`      VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_worker_code` (`code`)
) ENGINE = InnoDB COMMENT = '园区作业人员表';

INSERT INTO `wms_worker` (`code`, `name`, `phone`, `role`, `department`, `area`, `status`, `remark`) VALUES
('W-001', '周工', '13800000001', '叉车司机', '仓储部',   'B1原材料仓库', 'ON',  NULL),
('W-002', '郑工', '13800000002', '叉车司机', '仓储部',   'B2成品仓库',   'ON',  NULL),
('W-003', '冯工', '13800000003', '仓管员',   '仓储部',   'B区堆场',     'ON',  NULL),
('W-004', '张伟', '13800000004', '仓库主管', '仓储部',   'B区仓库',     'ON',  '负责B区整体运营'),
('W-005', '刘洋', '13800000005', '调度员',   '运输部',   '停车场',       'OFF', '本周调休');

-- 园区巡逻车
DROP TABLE IF EXISTS `wms_patrol_vehicle`;
CREATE TABLE `wms_patrol_vehicle` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL COMMENT '巡逻车编号',
    `name`        VARCHAR(64)  DEFAULT NULL COMMENT '名称',
    `driver`      VARCHAR(32)  DEFAULT NULL COMMENT '驾驶员',
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'PATROL' COMMENT 'PATROL/IDLE/CHARGING/MAINTAIN',
    `battery`     INT          NOT NULL DEFAULT 100 COMMENT '电量%',
    `route`       VARCHAR(128) DEFAULT NULL COMMENT '巡逻路线',
    `description` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_patrol_code` (`code`)
) ENGINE = InnoDB COMMENT = '园区巡逻车表';

INSERT INTO `wms_patrol_vehicle` (`code`, `name`, `driver`, `phone`, `status`, `battery`, `route`, `description`) VALUES
('PV-01', '巡逻车1号', '赵安', '13800000011', 'PATROL', 86, '园区外环', '园区外围环线巡逻'),
('PV-02', '巡逻车2号', '钱卫', '13800000012', 'PATROL', 92, '园区外环', '园区外围环线巡逻');

-- 巡检保安（携带定位器）
DROP TABLE IF EXISTS `wms_guard`;
CREATE TABLE `wms_guard` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(32)  NOT NULL COMMENT '保安编号',
    `name`        VARCHAR(32)  NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ON_PATROL' COMMENT 'ON_PATROL/IDLE/OFF',
    `area`        VARCHAR(64)  DEFAULT NULL COMMENT '负责区域',
    `locator_no`  VARCHAR(32)  DEFAULT NULL COMMENT '定位器编号',
    `description` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guard_code` (`code`)
) ENGINE = InnoDB COMMENT = '巡检保安表';

INSERT INTO `wms_guard` (`code`, `name`, `phone`, `status`, `area`, `locator_no`, `description`) VALUES
('SEC-01', '孙强', '13800000021', 'ON_PATROL', '园区主环线', 'LOC-001', '主环线步行巡检'),
('SEC-02', '李勇', '13800000022', 'ON_PATROL', '停车场环线', 'LOC-002', '停车场步行巡检'),
('SEC-03', '周明', '13800000023', 'ON_PATROL', '园区主环线', 'LOC-003', '主环线步行巡检'),
('SEC-04', '吴磊', '13800000024', 'IDLE',      '园区入口',   'LOC-004', '入口岗亭值守');
