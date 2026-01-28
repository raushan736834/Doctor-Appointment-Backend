package com.harsh.AppointDoctor.scheduler;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Services.SlotGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationScheduler {

    private final DoctorRepo doctorRepo;
    private final SlotGenerationService slotGenerationService;

    /**
     * Runs every day at 2 AM
     * Generates slots for next 14 days
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateSlotsDaily() {
        List<Doctor> doctors = doctorRepo.findAll();

        for (Doctor doctor : doctors) {
            try {
                slotGenerationService.generateSlotsForDoctor(
                        doctor.getDoctorId(),
                        14
                );
            } catch (Exception e) {
                log.error("Failed to generate slots for doctor {}",
                        doctor.getDoctorId(), e);
            }
        }
    }
}

