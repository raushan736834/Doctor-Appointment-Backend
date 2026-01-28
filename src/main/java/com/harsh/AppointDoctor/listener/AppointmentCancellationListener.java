package com.harsh.AppointDoctor.listener;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.NotificationService;
import com.harsh.AppointDoctor.events.AppointmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AppointmentCancellationListener {

    private final NotificationService notificationService;
    private final MailService emailService;

    @Async
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancellation(AppointmentCancelledEvent event) {

        // ================= DOCTOR =================
        if ("PATIENT".equalsIgnoreCase(event.cancelledBy())) {

            String doctorMessage = String.format(
                    "Patient %s cancelled the appointment scheduled for %s",
                    event.patientName(),
                    event.appointmentDate()
            );

            notificationService.sendNotification(
                    event.doctorEmail(),
                    "Appointment Cancelled",
                    doctorMessage,
                    AppointmentStatus.CANCELLED,
                    event.appointmentId().toString()
            );

            emailService.sendSimpleEmail(
                    event.doctorEmail(),
                    "Appointment Cancelled",
                    doctorMessage
            );
        }

        // ================= PATIENT =================
        if ("DOCTOR".equalsIgnoreCase(event.cancelledBy())) {

            String patientMessage = String.format(
                    "Your appointment with %s on %s was cancelled. Reason: %s",
                    event.doctorName(),
                    event.appointmentDate(),
                    event.reason()
            );

            notificationService.sendNotification(
                    event.patientEmail(),
                    "Appointment Cancelled",
                    patientMessage,
                    AppointmentStatus.CANCELLED,
                    event.appointmentId().toString()
            );

            emailService.sendSimpleEmail(
                    event.patientEmail(),
                    "Appointment Cancelled",
                    patientMessage
            );
        }
    }
}
