package com.webproject.controller;

import com.webproject.pojo.Image;
import com.webproject.pojo.Result;
import com.webproject.service.ImageService;
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
@RequestMapping("/image")

public class ImageResController {
    
    @Autowired
    private ImageService imageService;


    @GetMapping
    public Result getAllImage() {
            List<Image> images = imageService.getImage();
            return Result.success(images);
    }


/**
 * 自定义构造一个HTTP响应
 * 
 * 一个完整的HTTP响应包括：
 * 1. 状态
 *    - 协议版本：HTTP/1.1
 *    - 状态码：200, 404, 500 等
 *    - 状态描述：OK, Not Found, Internal Server Error 等
 * 
 * 2. 响应头（Response Headers）
 *    - Content-Type：响应体的 MIME 类型（如 image/jpeg, video/mp4）
 *    - Content-Length：响应体的字节大小
 *    - Cache-Control：缓存策略
 *    - Access-Control-Allow-Origin：跨域控制
 * 
 * 

 * 
 *   响应体（Response Body）
 *    - 实际的数据内容（文件流、JSON、HTML、文本等）
 * 
 * ResponseEntity 结构：
 * - body：响应体内容（Resource/String/Object/byte[] 等）
 * - headers：HttpHeaders 对象，包含所有响应头
 * - status：HttpStatus 枚举，表示状态码
 */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImageById(@PathVariable Integer id) {



            // 获取图片信息
            List<Image> images = imageService.getImageById(id);
            if (images.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Image image = images.get(0);
            String filePath = image.getUrl(); // 本地文件路径 E:\resource\I1.jpg
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 使用 FileSystemResource 进行流式传输
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(file.length());
            ResponseEntity<Resource> response = new ResponseEntity<>(resource, headers, HttpStatus.OK);
            return response;
            

    }




}
