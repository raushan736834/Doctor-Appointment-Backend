package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.Users;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentBookingRepo extends JpaRepository<AppointmentBooking,String> {
    List<AppointmentBooking> findByEmail(String email);
    List<AppointmentBooking> findByDoctorIdAndDate(String doctorId, String date);

    @Query("SELECT a FROM AppointmentBooking a WHERE a.email = :email AND (a.status = false AND a.date >= :today)")
    List<AppointmentBooking> findActiveOrFutureAppointmentsByEmail(
        @Param("email") String email,
        @Param("today") String today // Pass today's date as "yyyy-MM-dd"
    );
}