package com.harsh.AppointDoctor.Models;

import com.harsh.AppointDoctor.Enums.SlotStatus;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "appointment_slot",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"doctor_id", "slot_date", "slot_time"}
        ),
        indexes = {
                @Index(name = "idx_slot_doctor_date", columnList = "doctor_id, slot_date")
        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlot {

    @Id
    private String slotId;

    @PrePersist
    public void generateId() {
        if (this.slotId == null) {
            this.slotId = UUID.randomUUID().toString();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @OneToMany(mappedBy = "slot", fetch = FetchType.LAZY)
    private List<AppointmentBooking> appointments = new ArrayList<>();
}
