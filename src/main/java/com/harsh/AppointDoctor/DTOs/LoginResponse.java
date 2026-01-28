package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Role;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String accessToken;
    private String email;
    private List<Role> roles;
    private String fullname;
    private String refreshToken;

    // Constructor
    public LoginResponse(String message, String accessToken, String refreshToken,String email, List<
            Role> roles,String fullname) {
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}

