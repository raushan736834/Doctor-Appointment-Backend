package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.Users;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentBookingRepo extends JpaRepository<AppointmentBooking,String> {
    List<AppointmentBooking> findByEmail(String email);
    List<AppointmentBooking> findByDoctorIdAndDate(int doctorId, String date);
}

