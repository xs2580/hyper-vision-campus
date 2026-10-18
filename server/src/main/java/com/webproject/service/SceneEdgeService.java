package com.webproject.service;

import com.webproject.pojo.SceneEdge;

import java.util.List;

public interface SceneEdgeService {
    
    /**
     * 获取所有边（前端用于构建邻接表）
     */
    List<SceneEdge> getAllEdges();
}
