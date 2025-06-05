package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private DoctorProfile doctor;

    private String email;
    private boolean status;
    private String reason;
    private String cancelledBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "slot")
    private String time;
    private String period;

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
}