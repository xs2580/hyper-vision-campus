package com.webproject.pojo;

import lombok.Data;

import org.springframework.stereotype.Component;

/**
 * 聊天请求DTO
 */
@Data
@Component
public class ChatRequest {

    private String question;
    private Boolean stream = true;
    private String sessionId;
    private String userId;
}

