package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    private String receiptId;
    private String paymentId;
    private String orderId;
    private String doctorId;
    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointmentId")
    private AppointmentBooking appointmentBooking;
}
