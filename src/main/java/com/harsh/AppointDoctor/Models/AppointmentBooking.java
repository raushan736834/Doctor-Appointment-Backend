package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentBooking {
    @Id
    private String appointmentId;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private String reason;
    private String cancelledBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "slot")
    private String time;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "appointmentDate")
    private String date;

    @Column(name = "patientName")
    private String fullName;

    private String patientEmail;

    @Column(name = "paymentType")
    private String selectedPayment;

    private String phone;
    private String selectedPatient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctorId")
    private Doctor doctor;

    @OneToOne(mappedBy = "appointmentBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Payment payment;
}
