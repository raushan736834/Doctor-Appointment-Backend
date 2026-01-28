package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.Payment;
import lombok.Data;

@Data
public class AppointmentBookingRequest {
    private String slotId;
    private AppointmentBooking formData;
    private Payment payment;
}
