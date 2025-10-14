package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class DoctorClinicInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clinicName;
    private String clinicType;
    private String clinicPhone;
    private String clinicEmail;
    private String establishedYear;
    private String clinicAddress;
    private String clinicCity;
    private String clinicState;
    private String clinicPincode;
    private String consultationDuration;

    @ElementCollection
    @CollectionTable(name = "doctor_clinic_hours", // change to something unique
            joinColumns = @JoinColumn(name = "doctor_clinic_id"))
    private List<OperationHours> operatingHours = new ArrayList<>();


    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @ToString.Exclude
    private Doctor doctor;
}
