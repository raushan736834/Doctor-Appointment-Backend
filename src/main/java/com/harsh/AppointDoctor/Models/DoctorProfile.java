package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class DoctorProfile {
    @Id
    private String id;
    private String email;
    private String doctorName;
    private String specialization;
    private int consultationFees;
    private int experienceYears;
    private String profilePhoto;
    private String locality;
    private String clinicName;
    private String city;
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    private List<Qualification> qualifications = new ArrayList<>();
}
