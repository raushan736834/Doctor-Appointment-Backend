package com.harsh.AppointDoctor.Models;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private AppointmentStatus type;

    @Column(nullable = false)
    private boolean isRead = false;

    public Notification() {
    }

    public Notification(String userEmail, String title, String message, AppointmentStatus type, String appointmentId) {
        this.userEmail = userEmail;
        this.title = title;
        this.type = type;
        this.message = message;
        this.appointmentId = appointmentId;
    }

    @Column(nullable = false)
    private Date createdAt = new Date();

    @Column
    private String appointmentId;
}
