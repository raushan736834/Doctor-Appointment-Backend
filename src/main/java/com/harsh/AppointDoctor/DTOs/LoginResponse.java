package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Role;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private int status;
    private String accessToken;
    private String email;
    private List<Role> roles;
    private String fullname;
    private String refreshToken;
    private String doctorId;
    private AccountStatus accountStatus;

    // Constructor
    public LoginResponse(String message, int status, String accessToken, String refreshToken,String email, List<
            Role> roles,String fullname) {
        this.message = message;
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
    }

    public LoginResponse(String message, int status, String accessToken,String refreshToken,
                         String email, List<Role> roles,String fullname, String doctorId, AccountStatus accountStatus) {
        this.message = message;
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.roles = roles;
        this.fullname = fullname;
        this.doctorId = doctorId;
        this.accountStatus = accountStatus;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", doctorId='" + doctorId + '\'' +
                ", accountStatus=" + accountStatus +
                '}';
    }
}

