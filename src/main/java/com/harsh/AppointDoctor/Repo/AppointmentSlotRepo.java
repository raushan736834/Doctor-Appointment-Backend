package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.AppointmentSlot;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentSlotRepo extends JpaRepository<AppointmentSlot, String> {
    @Query("""
        SELECT s FROM AppointmentSlot s
        WHERE s.doctor.doctorId = :doctorId
          AND s.slotDate = :date
          AND s.status = 'AVAILABLE'
          AND (
                :date <> :today
                OR s.slotTime > :now
              )
    """)
    List<AppointmentSlot> findAvailableSlots(
            @Param("doctorId") String doctorId,
            @Param("date") LocalDate date,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now
    );



    boolean existsByDoctor_DoctorIdAndSlotDateAndSlotTime(
            String doctorId,
            LocalDate slotDate,
            LocalTime slotTime
    );
}
