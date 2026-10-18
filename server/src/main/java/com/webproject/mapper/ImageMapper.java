package com.webproject.mapper;

import com.webproject.pojo.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageMapper {
    
    @Select("SELECT id, url, description, create_time as createTime, update_time as updateTime FROM image ORDER BY id ASC")
    List<Image> getAll();
    
    @Select("SELECT id, url, description, create_time as createTime, update_time as updateTime FROM image_reverse ORDER BY id ASC")
    List<Image> getAllReverse();
    
    @Select("SELECT id, url, description, create_time as createTime, update_time as updateTime FROM image WHERE id = #{id}")
    List<Image> getById(Integer id);
    
    @Select("SELECT id, url, description, create_time as createTime, update_time as updateTime FROM image_reverse WHERE id = #{id}")
    List<Image> getByIdReverse(Integer id);
}
