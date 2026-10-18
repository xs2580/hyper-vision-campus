package com.webproject.pojo;

import lombok.Data;

/**
 * 会话响应DTO
 */
@Data
public class SessionResponse {
    
    /**
     * 响应代码，0表示成功
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 会话数据（单个会话或会话列表）
     */
    private Object data;
}

