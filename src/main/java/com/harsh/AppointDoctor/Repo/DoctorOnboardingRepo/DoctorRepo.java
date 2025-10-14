package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, String> {
    Doctor findByEmail(String email);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.documents WHERE d.email = :email")
    Optional<Doctor> findByEmailWithDocuments(@Param("email") String email);

}
