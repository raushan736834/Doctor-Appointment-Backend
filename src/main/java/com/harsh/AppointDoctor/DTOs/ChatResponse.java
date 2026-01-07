package com.harsh.AppointDoctor.DTOs;

import lombok.Data;
import java.util.Map;

@Data
public class ChatResponse {
    private String response;
    private String intent;
    private String action_taken;
    private Map<String, Object> data;
}
