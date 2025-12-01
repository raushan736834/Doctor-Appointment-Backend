package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentBookingRepo extends JpaRepository<AppointmentBooking,String> {
    List<AppointmentBooking> findByStatus(AppointmentStatus status);

    List<AppointmentBooking> findByEmail(String email);
//    List<AppointmentBooking> findByDoctorIdAndDateAndStatus(String doctorId, String date, AppointmentStatus status);


    @Query("SELECT a FROM AppointmentBooking a " +
            "WHERE a.email = :email " +
            "AND a.status IN (:statuses) " +
            "AND a.date >= :today")
    List<AppointmentBooking> findActiveOrFutureAppointmentsByEmail(
            @Param("email") String email,
            @Param("today") String today,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    long countByDoctor_DoctorId(String doctorId);

    List<AppointmentBooking> findByDoctor_DoctorIdAndDateAndStatusIn(String doctorId, String date, List<AppointmentStatus> status);
    Page<AppointmentBooking> findByDoctor_DoctorIdAndDateAndStatusIn(String doctorId, String date, List<AppointmentStatus> status,Pageable pageable);


}