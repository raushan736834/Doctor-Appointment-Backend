package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String fullname;
    private List<Role> roles;
    private String accessToken;
}
