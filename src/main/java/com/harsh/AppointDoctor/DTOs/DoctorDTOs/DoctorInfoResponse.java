package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import com.harsh.AppointDoctor.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfoResponse {
    private String email;
    private String fullname;
    private List<Role> roles;         // or List<String> if you prefer
    private String doctorId;
    private String  accountStatus;
    private String accessToken;
    private int consultationFees;
}
