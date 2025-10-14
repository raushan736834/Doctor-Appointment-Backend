package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
public class DoctorEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String schoolName;
    private String degreeName;
    private int completionYear;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @ToString.Exclude
    private Doctor doctor;
}
