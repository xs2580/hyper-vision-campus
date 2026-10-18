package com.webproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SceneEdge {
    private Integer id;              // 边ID
    private Integer startId;         // 起点场景ID
    private Integer endId;           // 终点场景ID
    private Integer videoId;         // 从起点到终点的视频ID
    private Float angle;             // 热点角度（用于在全景图上定位）
    private String createTime;       // 创建时间
    private String updateTime;       // 更新时间
}
