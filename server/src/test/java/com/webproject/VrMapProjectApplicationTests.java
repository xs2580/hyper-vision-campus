package com.webproject;

import com.webproject.mapper.ImageMapper;
import com.webproject.pojo.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class VrMapProjectApplicationTests {

//    @Test
//    void contextLoads() {
//    }
@Autowired
private ImageMapper imageMapper;
    @Test
    void contextLoads() {
    }
    @Test
    void ImageTest()
    {
        List<Image> result=imageMapper.getAll();
        for(Image i:result)
        {
            System.out.println(i);
        }

    }
}
