package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import lombok.Data;

@Data
public class DoctorEducationalDetailsResponse {
    private String schoolName;
    private String degreeName;
    private int completionYear;
}
