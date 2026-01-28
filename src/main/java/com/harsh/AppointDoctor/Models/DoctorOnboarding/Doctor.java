package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
//    @UuidGenerator
    private String doctorId;
    @Lob
    private byte[] profileImage;
//    private String profileImage;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String dob;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(nullable = false)
    private int totalRatings = 0;

    @OneToOne(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private DoctorProfessional professional;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<DoctorEducation> doctorEducation = new ArrayList<>();

    @OneToOne(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private DoctorClinicInfo clinicInfos;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<DoctorDocument> documents = new ArrayList<>();
}
