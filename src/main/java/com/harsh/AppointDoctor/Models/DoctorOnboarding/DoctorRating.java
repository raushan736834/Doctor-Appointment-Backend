package com.harsh.AppointDoctor.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReview {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID reviewId;

    @PrePersist
    public void generateId() {
        if (this.reviewId == null) {
            this.reviewId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, columnDefinition = "BINARY(16)")
    private AppointmentBooking appointment;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 1000)
    private String reviewText;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isEdited = false;
}