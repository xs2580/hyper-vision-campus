package com.webproject.service.impl;

import com.webproject.mapper.SceneEdgeMapper;
import com.webproject.pojo.SceneEdge;
import com.webproject.service.SceneEdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SceneEdgeServiceImpl implements SceneEdgeService {

    @Autowired
    private SceneEdgeMapper sceneEdgeMapper;

    @Override
    public List<SceneEdge> getAllEdges() {
        return sceneEdgeMapper.getAll();
    }
}
