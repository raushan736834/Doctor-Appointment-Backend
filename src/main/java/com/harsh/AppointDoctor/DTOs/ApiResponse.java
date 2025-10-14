package com.harsh.AppointDoctor.DTOs;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int status;

    // Static factory method for a successful response
    public static <T> ApiResponse<T> success(T data, String message, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setStatus(status);
        return response;
    }

    // Static factory method for an error response
    public static <T> ApiResponse<T> error(String message, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(status);
        return response;
    }
}
