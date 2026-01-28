package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.DTOs.UserAppointmentResponse;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.AppointmentSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentBookingRepo
        extends JpaRepository<AppointmentBooking, UUID> {

    /* ---------------- BASIC ---------------- */

    List<AppointmentBooking> findByStatus(AppointmentStatus status);

    List<AppointmentBooking> findByEmail(String email);

    @Query("""
    SELECT new com.harsh.AppointDoctor.DTOs.UserAppointmentResponse(
        b.appointmentId,
        CONCAT(d.firstName, ' ', d.lastName),
        p.specialization,
        s.slotDate,
        s.slotTime,
        p.consultationFees,
        p.yearOfExp,
        c.clinicAddress,
        c.clinicName,
        b.status,
        b.selectedPayment,
        d.doctorId,
        r.rating,
        r.review,
        r.anonymous
    )
    FROM AppointmentBooking b
    JOIN b.slot s
    JOIN s.doctor d
    JOIN d.professional p
    JOIN d.clinicInfos c
    LEFT JOIN DoctorRating r ON r.appointment = b
    WHERE b.email = :email
      AND s.slotDate >= :today
      AND b.status IN :statuses
""")
    List<UserAppointmentResponse> findActiveOrFutureAppointmentsByEmail(
            @Param("email") String email,
            @Param("today") LocalDate today,
            @Param("statuses") List<AppointmentStatus> statuses
    );



    /* -------- COUNT BOOKINGS FOR A DOCTOR -------- */

    long countBySlot_Doctor_DoctorId(String doctorId);

    /* -------- BOOKINGS FOR A DOCTOR ON A DATE -------- */

    List<AppointmentBooking>
    findBySlot_Doctor_DoctorIdAndSlot_SlotDateAndStatusIn(
            String doctorId,
            LocalDate slotDate,
            List<AppointmentStatus> status
    );

    @Query("""
    SELECT new com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO(
        b.appointmentId,
        b.fullName,
        b.patientEmail,
        b.phone,
        s.slotDate,
        s.slotTime,
        b.status,
        b.selectedPayment,
        p.consultationFees
    )
    FROM AppointmentBooking b
    JOIN b.slot s
    JOIN s.doctor d
    JOIN d.professional p
    WHERE d.doctorId = :doctorId
      AND s.slotDate BETWEEN :startDate AND :endDate
      AND b.status IN :statuses
""")
    Page<DoctorAppointmentResponseDTO> findDoctorAppointmentsBetweenDates(
            @Param("doctorId") String doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AppointmentStatus> statuses,
            Pageable pageable
    );


    @Query("""
    SELECT new com.harsh.AppointDoctor.DTOs.UserAppointmentResponse(
        b.appointmentId,
        CONCAT(d.firstName, ' ', d.lastName),
        p.specialization,
        s.slotDate,
        s.slotTime,
        p.consultationFees,
        p.yearOfExp,
        c.clinicAddress,
        c.clinicName,
        b.status,
        b.selectedPayment,
        d.doctorId,
        r.rating,
        r.review,
        r.anonymous
    )
    FROM AppointmentBooking b
    JOIN b.slot s
    JOIN s.doctor d
    JOIN d.professional p
    JOIN d.clinicInfos c
    LEFT JOIN DoctorRating r ON r.appointment = b
    WHERE b.email = :email
      AND s.slotDate < :today
      AND b.status IN :statuses
    ORDER BY s.slotDate DESC, s.slotTime DESC
""")
    List<UserAppointmentResponse> findPastAppointmentsByEmail(
            @Param("email") String email,
            @Param("today") LocalDate today,
            @Param("statuses") List<AppointmentStatus> statuses
    );


}
