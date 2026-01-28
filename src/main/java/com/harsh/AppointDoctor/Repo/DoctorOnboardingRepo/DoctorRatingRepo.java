package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRatingRepo extends JpaRepository<DoctorRating, UUID> {

    boolean existsByAppointment(AppointmentBooking appointment);

    List<DoctorRating> findByDoctor_DoctorIdOrderByCreatedAtDesc(String doctorId);
}

