package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "doctor_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = "appointment_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_email")
    private Users user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id")
    private AppointmentBooking appointment;

    private int rating;
    private String review;
    private boolean anonymous;
    private LocalDateTime createdAt;
}

