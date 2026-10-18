package com.webproject.controller;

import com.webproject.pojo.Result;
import com.webproject.pojo.Video;
import com.webproject.service.videoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/video")

public class VideoResController {
    @Autowired
    private videoService videoservice;

    @GetMapping
    public Result getAllVideo(){
       
            List<Video> videos = videoservice.getVideo();
            return Result.success(videos);
     
    }



    @GetMapping("/{id}")
    public ResponseEntity<Resource> getVideoById(@PathVariable Integer id) {
     
            // 获取视频信息
            List<Video> videos = videoservice.getVideoById(id);
            if (videos.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Video video = videos.get(0);
            String filePath = video.getUrl(); // 本地文件路径 E:\resource\V1.mp4
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 使用 FileSystemResource 进行流式传输
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.setContentLength(file.length());
            
            ResponseEntity<Resource> response = new ResponseEntity<>(resource, headers, HttpStatus.OK);
            return response;
     
    }


}
