package com.harsh.AppointDoctor.DTOs;

import lombok.Data;

@Data
public class RescheduleAppointmentReqDTO {
    private String appointmentId;
    private String newDate;
    private String newTimeSlot;
}
