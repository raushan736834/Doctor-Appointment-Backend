package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorProfessional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorProfessionalRepo extends JpaRepository<DoctorProfessional, Long> {
}
