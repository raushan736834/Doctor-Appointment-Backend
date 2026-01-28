package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAppointmentResponse {
    private UUID appointmentId;
    private String doctorName;
    private String specialization;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private int consultationFees;
    private String yearOfExp;
    private String clinicAddress;
    private String clinicName;
    private AppointmentStatus status;
    private String paymentType;
    private String doctorId;
    // ⭐ Rating related (nullable)
    private Integer rating;
    private String review;
    private Boolean anonymous;
}
