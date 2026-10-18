package com.webproject.service.impl;

import com.webproject.mapper.VideoMapper;
import com.webproject.pojo.Video;
import com.webproject.service.videoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class videoServiceImpl implements videoService {

    @Autowired
    private VideoMapper videomapper;
    @Override
    public List<Video> getVideo() {
        return videomapper.getAll();
    }

    @Override
    public List<Video> getVideoReverse() {
        return videomapper.getAllReverse();
    }

    @Override
    public List<Video> getVideoById(Integer id) {
        return videomapper.getById(id);
    }

    @Override
    public List<Video> getVideoById_reverse(Integer id) {
        return videomapper.getByIdReverse(id);
    }
}
