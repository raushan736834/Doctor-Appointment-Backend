package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qualification;
    private String college;
    private int completionYear;
    private boolean isQualificationValidInCountry;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference
    @ToString.Exclude
    private DoctorProfile doctor;
}

