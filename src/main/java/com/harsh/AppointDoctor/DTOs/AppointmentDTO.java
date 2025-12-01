package com.harsh.AppointDoctor.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentDTO {
    private String appointmentId;
    private String reason;
    private String cancelledBy;
    private String newDate;
    private String newTime;
    private String newPeriod;
}
