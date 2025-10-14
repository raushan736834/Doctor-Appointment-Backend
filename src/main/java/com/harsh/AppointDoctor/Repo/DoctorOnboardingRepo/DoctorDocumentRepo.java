package com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorDocumentRepo extends JpaRepository<DoctorDocument, Long> {
}
