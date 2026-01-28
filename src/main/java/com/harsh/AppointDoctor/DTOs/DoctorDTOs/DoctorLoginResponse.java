package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DoctorLoginResponse {
    private String fullname;
    private String email;
    private String doctorId;
    private String accessToken;
    private List<Role> roles;
    private AccountStatus AccountStatus;
    private String message;
    private String refreshToken;
    private int consultationFees;
}
