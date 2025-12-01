package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, String> {
    Doctor findByEmail(String email);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.documents WHERE d.email = :email")
    Optional<Doctor> findByEmailWithDocuments(@Param("email") String email);

    @Query("""
    SELECT DISTINCT d FROM Doctor d 
    LEFT JOIN d.clinicInfos c 
    LEFT JOIN d.professional p
    WHERE LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(p.specialization) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(c.clinicName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(d.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Doctor> searchDoctorDetails(@Param("keyword") String keyword);

    Doctor findByDoctorIdIgnoreCase(String id);

}
