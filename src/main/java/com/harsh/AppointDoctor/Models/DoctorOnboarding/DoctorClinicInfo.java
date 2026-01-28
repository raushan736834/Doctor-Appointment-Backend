package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(unique = true)
    private String clinicPhone;
    @Column(unique = true)
    private String clinicEmail;
    private String establishedYear;
    private String clinicAddress;
    private String clinicCity;
    private String clinicState;
    private String clinicPincode;
    private String consultationDuration;

    @ElementCollection(fetch = FetchType.EAGER)  // Add EAGER fetch
    @CollectionTable(name = "doctor_clinic_hours",
            joinColumns = @JoinColumn(name = "doctor_clinic_id"))
    private List<OperationHours> operatingHours = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Doctor doctor;
}