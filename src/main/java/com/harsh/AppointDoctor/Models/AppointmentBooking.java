package com.harsh.AppointDoctor.Models;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointment_booking")
public class AppointmentBooking {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID appointmentId;

    @PrePersist
    public void generateId() {
        if (this.appointmentId == null) {
            this.appointmentId = UUID.randomUUID();
        }
    }

    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private String reason;
    private String cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlot slot;


    private String fullName;
    private String patientEmail;
    private String selectedPayment;
    private String phone;
    private String selectedPatient;

    @OneToOne(mappedBy = "appointmentBooking", cascade = CascadeType.ALL)
    private Payment payment;
}
