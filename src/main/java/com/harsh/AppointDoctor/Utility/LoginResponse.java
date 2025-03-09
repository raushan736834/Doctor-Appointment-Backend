package com.harsh.AppointDoctor.Utility;

import lombok.*;

import java.util.List;


@Data
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private int status;
    private String token;
    private String email;
    private String roles;
    private String fullname;

    // Constructor
    public LoginResponse(String message, int status, String token, String email, String roles,String fullname) {
        this.message = message;
        this.status = status;
        this.token = token;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}

