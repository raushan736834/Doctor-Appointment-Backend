package com.harsh.AppointDoctor.events;

import java.time.LocalDate;
import java.util.UUID;

public record AppointmentCancelledEvent(
        UUID appointmentId,
        String cancelledBy,
        String reason,
        String patientEmail,
        String doctorEmail,
        String patientName,
        String doctorName,
        LocalDate appointmentDate
) {}

