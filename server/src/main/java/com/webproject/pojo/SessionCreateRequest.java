package com.webproject.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 创建会话请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class SessionCreateRequest {

    private String name;
    @JsonProperty("user_id")
    private String userId;
}

