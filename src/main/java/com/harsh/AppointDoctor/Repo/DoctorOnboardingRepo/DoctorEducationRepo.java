package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorEducationRepo extends JpaRepository<DoctorEducation, Long> {
}
