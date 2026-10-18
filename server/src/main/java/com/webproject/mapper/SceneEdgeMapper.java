package com.webproject.mapper;

import com.webproject.pojo.SceneEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SceneEdgeMapper {
    
    /**
     * 获取所有边（前端用于构建邻接表）
     */
    @Select("SELECT id, start_id as startId, end_id as endId, video_id as videoId, angle, create_time as createTime, update_time as updateTime FROM scene_edge")
    List<SceneEdge> getAll();
}
