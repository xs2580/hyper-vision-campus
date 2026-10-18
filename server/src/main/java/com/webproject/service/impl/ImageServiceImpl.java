package com.webproject.service.impl;

import com.webproject.mapper.ImageMapper;
import com.webproject.pojo.Image;
import com.webproject.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageMapper imageMapper;

    @Override
    public List<Image> getImage() {
        return imageMapper.getAll();
    }

    @Override
    public List<Image> getImageReverse() {
        return imageMapper.getAllReverse();
    }

    @Override
    public List<Image> getImageById(Integer id) {
        return imageMapper.getById(id);
    }

    @Override
    public List<Image> getImageById_reverse(Integer id) {
        return imageMapper.getByIdReverse(id);
    }
}
