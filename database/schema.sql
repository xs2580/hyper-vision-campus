-- 超维新视界 · 数据库结构
-- MySQL 8.0+，字符集 utf8mb4
--
-- 说明：项目把全景素材放在磁盘上（或对象存储），数据库只存路径与场景关系。
-- image.url / video.url 记录素材的绝对路径，后端读取后以流的方式返回给前端。

CREATE DATABASE IF NOT EXISTS `hyper_vision`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `hyper_vision`;

-- 全景场景（每张 equirectangular 全景图 = 一个可停留的节点）
CREATE TABLE IF NOT EXISTS `image` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '场景 ID，同时作为导航图节点编号',
  `url`         VARCHAR(500) NOT NULL COMMENT '全景图路径，例如 E:/resource/I1.jpg',
  `description` VARCHAR(200) NOT NULL COMMENT '场景名称，会显示在全景标签与地图上',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '全景场景表';

-- 过渡视频（两个场景之间播放的行走镜头）
CREATE TABLE IF NOT EXISTS `video` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `url`         VARCHAR(500) NOT NULL COMMENT '过渡视频路径，例如 E:/resource/V1.mp4',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '过渡视频表';

-- 场景边：有向边，start_id --video_id--> end_id
-- 前端读全表后在内存里建成邻接表（Map<startId, Edge[]>），用于生成热点标签与路径查询
CREATE TABLE IF NOT EXISTS `scene_edge` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `start_id`    INT UNSIGNED NOT NULL COMMENT '起点场景 ID，关联 image.id',
  `end_id`      INT UNSIGNED NOT NULL COMMENT '终点场景 ID，关联 image.id',
  `video_id`    INT UNSIGNED     NULL COMMENT '该方向上的过渡视频 ID，关联 video.id；为空表示直接切图',
  `angle`       FLOAT        NOT NULL DEFAULT 0 COMMENT '标签方位角（度）。0=正前方，正值向右，负值向左',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_start` (`start_id`),
  KEY `idx_end` (`end_id`),
  KEY `idx_video` (`video_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '场景连接边（导航图）';

-- 反向游览预留表：结构与正向表一致，用于「原路返回」时播放倒放视频。
-- 当前版本接口尚未接入，保留以便扩展。
CREATE TABLE IF NOT EXISTS `image_reverse` LIKE `image`;
CREATE TABLE IF NOT EXISTS `video_reverse` LIKE `video`;
