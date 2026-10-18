package com.webproject.controller;

import com.webproject.pojo.Result;
import com.webproject.pojo.SceneEdge;
import com.webproject.service.SceneEdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/edge")

public class SceneEdgeController {

    @Autowired
    private SceneEdgeService sceneEdgeService;

    /**
     * 获取所有边（前端用于构建邻接表）
     * GET /scene-edge
     */
    @GetMapping
    public Result getAllEdges() {
        List<SceneEdge> edges = sceneEdgeService.getAllEdges();
        return Result.success(edges);
    }
}
