package com.webproject.service;

import com.webproject.pojo.Image;

import java.util.List;

public interface ImageService {
    List<Image> getImage();
    List<Image> getImageReverse();
    List<Image> getImageById(Integer id);
    List<Image> getImageById_reverse(Integer id);
}
