package com.harsh.AppointDoctor.DTOs;

import lombok.Data;
import java.util.Map;

@Data
public class ChatRequest {
    private String userId;
    private String message;
    private Map<String, Object> context;
}
