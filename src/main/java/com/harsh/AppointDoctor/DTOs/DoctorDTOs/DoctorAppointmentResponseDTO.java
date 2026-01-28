package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

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
public class DoctorAppointmentResponseDTO {
    private UUID appointmentId;

    private String patientName;
    private String patientEmail;
    private String phone;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private AppointmentStatus status;

    private String paymentType;
    private int consultationFees;
}
