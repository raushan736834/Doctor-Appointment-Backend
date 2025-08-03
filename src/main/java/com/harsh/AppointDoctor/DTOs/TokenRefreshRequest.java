package com.harsh.AppointDoctor.DTOs;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
