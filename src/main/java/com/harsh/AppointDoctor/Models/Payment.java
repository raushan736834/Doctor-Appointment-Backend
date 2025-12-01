package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.harsh.AppointDoctor.Enums.RefundStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {
    @Id
    private String receiptId;
    private String paymentId;
    private String orderId;
    private String doctorId;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NOT_REQUIRED;
    private String refundId;

    private int refundRetryCount = 0;
    private LocalDateTime lastRetryAt;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointmentId")
    private AppointmentBooking appointmentBooking;
}
