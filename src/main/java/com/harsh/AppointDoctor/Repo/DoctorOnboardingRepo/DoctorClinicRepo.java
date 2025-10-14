package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorClinicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorClinicRepo extends JpaRepository<DoctorClinicInfo,Long> {
}
