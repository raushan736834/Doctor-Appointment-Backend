package com.harsh.AppointDoctor.DTOs;

import lombok.*;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private int status;
    private String token;
    private String email;
    private String roles;
    private String fullname;
    private String refreshToken;
    private String doctorId;

    // Constructor
    public LoginResponse(String message, int status, String token,String refreshToken ,String email, String roles,String fullname) {
        this.message = message;
        this.status = status;
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
    }

    public LoginResponse(String message, int status, String token,String refreshToken ,String email, String roles,String fullname, String doctorId) {
        this.message = message;
        this.status = status;
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
        this.doctorId = doctorId;
    }


    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}

