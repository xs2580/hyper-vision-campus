-- 超维新视界 · 演示数据（3 个场景 + 3 条边，可直接跑通漫游与导航）
-- 使用前先把下面三个路径改成你本机的素材目录。
-- 素材要求：
--   * 全景图：equirectangular 投影，建议 2:1（如 4096x2048 / 8192x4096）
--   * 过渡视频：全景相机录制后同样以 equirectangular 投影导出，H.264 MP4

USE `hyper_vision`;

INSERT INTO `image` (`id`, `url`, `description`) VALUES
  (1, 'E:/resource/I1.jpg', '东门'),
  (2, 'E:/resource/I2.jpg', '东门广场'),
  (3, 'E:/resource/I3.jpg', '中心广场');

INSERT INTO `video` (`id`, `url`) VALUES
  (1, 'E:/resource/V1.mp4'),
  (2, 'E:/resource/V2.mp4'),
  (3, 'E:/resource/V3.mp4');

-- angle 是标签在画面里的方位：站在起点场景中，标签出现在哪个角度就填哪个值
INSERT INTO `scene_edge` (`start_id`, `end_id`, `video_id`, `angle`) VALUES
  (1, 2, 1,  60),   -- 东门 → 东门广场，标签在右前方
  (2, 3, 2,   0),   -- 东门广场 → 中心广场，正前方
  (3, 1, 3, -90);   -- 中心广场 → 东门，左手边
