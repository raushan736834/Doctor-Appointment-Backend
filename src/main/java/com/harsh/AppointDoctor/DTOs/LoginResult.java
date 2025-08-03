package com.harsh.AppointDoctor.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    private String email;
    private String roles;
    private String fullName;
}
