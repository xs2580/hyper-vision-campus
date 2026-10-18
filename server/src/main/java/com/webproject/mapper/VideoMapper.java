package com.webproject.mapper;

import com.webproject.pojo.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapper {
    
    @Select("SELECT id, url, create_time as createTime, update_time as updateTime FROM video ORDER BY id ASC")
    List<Video> getAll();
    
    @Select("SELECT id, url, create_time as createTime, update_time as updateTime FROM video_reverse ORDER BY id ASC")
    List<Video> getAllReverse();
    
    @Select("SELECT id, url, create_time as createTime, update_time as updateTime FROM video WHERE id = #{id}")
    List<Video> getById(Integer id);
    
    @Select("SELECT id, url, create_time as createTime, update_time as updateTime FROM video_reverse WHERE id = #{id}")
    List<Video> getByIdReverse(Integer id);
}
