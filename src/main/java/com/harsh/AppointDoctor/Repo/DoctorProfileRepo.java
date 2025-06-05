package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorProfileRepo extends JpaRepository<DoctorProfile,String> {
    @Query("SELECT d from DoctorProfile d WHERE "+
            "LOWER(d.doctorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DoctorProfile> searchDoctor(String keyword);

    @Query("SELECT d FROM DoctorProfile d WHERE LOWER(d.specialization) = LOWER(:specialization) AND LOWER(d.id) = LOWER(:id)")
    DoctorProfile findBySpecializationAndIdIgnoreCase(String specialization, String id);
}