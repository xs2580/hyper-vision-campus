package com.webproject.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class RagFlowchatRequest {
    private String question;
    private Boolean stream;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("user_id")
    private String userId;
}
