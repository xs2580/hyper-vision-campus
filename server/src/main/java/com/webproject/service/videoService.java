package com.webproject.service;

import com.webproject.pojo.Video;

import java.util.List;

public interface videoService {
    List<Video> getVideo();
    List<Video> getVideoReverse();
    List<Video> getVideoById(Integer id);
    List<Video> getVideoById_reverse(Integer id);
}
