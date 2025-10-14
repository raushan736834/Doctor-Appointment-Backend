package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
public class DoctorProfessional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String medicalLicenseNumber;
    private String yearOfExp;
    private String specialization;
    private String subSpeciality;
    private int consultationFees;
    private String currentHospital;
    private String bio;
    private String medicalCouncil;
    private String languageKnown;

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @ToString.Exclude
    private Doctor doctor;
}
