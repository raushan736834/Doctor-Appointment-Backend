package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DoctorReviewResponseDTO {
    private String patientName;
    private int rating;
    private String review;
    private LocalDateTime createdAt;
}

