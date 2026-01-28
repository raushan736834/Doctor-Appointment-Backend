package com.harsh.AppointDoctor.events;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRescheduledEvent(
        String userEmail,
        String doctorEmail,
        UUID appointmentId,
        String patientName,
        String doctorName,
        LocalDate oldDate,
        LocalDate newDate,
        LocalTime oldTime,
        LocalTime newTime,
        String rescheduledBy
) {}

