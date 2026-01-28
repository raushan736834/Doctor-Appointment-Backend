package com.harsh.AppointDoctor.scheduler;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Services.CommonAppointmentService;
import com.harsh.AppointDoctor.Services.UserAppointmentService;
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
    private final CommonAppointmentService appointmentService;

    @Scheduled(fixedRate = 6000000)
    public void fixPendingCancellations() {
        log.info("Running cancellation recovery job...");

        List<AppointmentBooking> pending =
                appointmentRepo.findByStatus(AppointmentStatus.CANCEL_PENDING);

        for (AppointmentBooking appointment : pending) {
            try {
                appointmentService.performCancellation(appointment);
                log.info("Recovered cancellation for appointment: {}", appointment.getAppointmentId());
            } catch (Exception e) {
                log.error("Retry failed for appointment {}", appointment.getAppointmentId(), e);
            }
        }
    }
}
