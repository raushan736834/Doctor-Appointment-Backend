package com.harsh.AppointDoctor.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AppointmentDTO {
    private UUID appointmentId;
    private String reason;
    private String cancelledBy;
}
