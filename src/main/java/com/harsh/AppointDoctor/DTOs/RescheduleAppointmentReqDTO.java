package com.harsh.AppointDoctor.DTOs;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class RescheduleAppointmentReqDTO {
    private UUID appointmentId;
    private String newSlotId;
    private String rescheduledBy;
}
