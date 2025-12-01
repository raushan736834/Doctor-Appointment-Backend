package com.harsh.AppointDoctor.scheduler;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationRecoveryScheduler {

    private final AppointmentBookingRepo appointmentRepo;
    private final AppointmentBookingService appointmentService;

    @Scheduled(fixedRate = 600000) // every 10 mins
    public void fixPendingCancellations() {
        log.info("Running cancellation recovery job...");

        List<AppointmentBooking> pending =
                appointmentRepo.findByStatus(AppointmentStatus.CANCEL_PENDING);

        for (AppointmentBooking appointment : pending) {
            try {
                appointmentService.cancelAppointmentOnly(appointment,appointment.getCancelledBy(),appointment.getReason());
                log.info("Recovered cancellation for appointment: {}", appointment.getAppointmentId());
            } catch (Exception e) {
                log.error("Retry failed for appointment {}", appointment.getAppointmentId(), e);
            }
        }
    }
}
