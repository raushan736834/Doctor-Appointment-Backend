package com.harsh.AppointDoctor.listener;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.NotificationService;
import com.harsh.AppointDoctor.Services.StompNotificationService;
import com.harsh.AppointDoctor.events.AppointmentCancelledEvent;
import com.harsh.AppointDoctor.events.AppointmentRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AppointmentRescheduleListener {

    private final NotificationService notificationService;
    private final MailService emailService;

    @Async
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentRescheduled(AppointmentRescheduledEvent event) {

        if(event.rescheduledBy().equals("DOCTOR")) {
            notificationService.sendNotification(event.userEmail(),"Appointment Rescheduled",
                    "Appointment with " + event.doctorName() + " has been rescheduled from "
                            + event.oldDate() + " " + event.oldTime() + " to "
                            + event.newDate() + " " + event.newTime(),
                    AppointmentStatus.RESCHEDULED,
                    event.appointmentId().toString()
            );
        } else {

           notificationService.sendNotification(event.doctorEmail(),"Appointment Rescheduled",
                    "Appointment with " + event.patientName() + " has been rescheduled from "
                            + event.oldDate() + " " + event.oldTime() + " to "
                            + event.newDate() + " " + event.newTime(),
                    AppointmentStatus.RESCHEDULED,
                    event.appointmentId().toString()
            );
        }

        emailService.sendSimpleEmail(
                event.userEmail(),
                "Appointment Rescheduled",
                "Your appointment has been rescheduled.\n\n"
                        + "Doctor: " + event.doctorName()
                        + "\nOld Date: " + event.oldDate()
                        + "\nNew Date: " + event.newDate()
                        + "\nOld Time: " + event.oldTime()
                        + "\nNew Time: " + event.newTime()
        );

        emailService.sendSimpleEmail(
                event.doctorEmail(),
                "Appointment Rescheduled",
                "Appointment is rescheduled By Patient.\n\n"
                        + "Patient: " + event.patientName()
                        + "\nOld Date: " + event.oldDate()
                        + "\nNew Date: " + event.newDate()
                        + "\nOld Time: " + event.oldTime()
                        + "\nNew Time: " + event.newTime()
        );
    }

    @Recover
    public void recover(Exception ex, AppointmentRescheduledEvent event) {
        // FINAL FAILURE — log or store for manual retry
        System.err.println("Rescheduling Notification permanently failed for " + event.appointmentId());
    }
}

