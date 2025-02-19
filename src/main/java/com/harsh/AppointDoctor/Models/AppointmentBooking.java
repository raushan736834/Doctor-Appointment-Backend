package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;

    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "slot")
    private String time;
    private String period;
    private String doctorName;
    private String specialization;

    private int consultation_fees;
    private String doctorProfileLink;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "appointmentDate")
    private String date;
    private int doctorId;
    @Column(name = "patientName")
    private String fullName;
    private String patientEmail;
    @Column(name = "paymentType")
    private String selectedPayment;
    private String phone;
    private String selectedPatient;
}
