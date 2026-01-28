package com.harsh.AppointDoctor.listener;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.NotificationService;
import com.harsh.AppointDoctor.events.AppointmentBookingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AppointmentBookingListener {

    private final NotificationService notificationService;
    private final MailService emailService;

    @Async
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentBookingEvent(AppointmentBookingEvent event) {

        String userEmailBody = String.format(
                "Dear %s,\n\nYour appointment with %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
                event.patientName(),
                event.doctorName(),
                event.specialization(),
                event.appointmentDate(),
                event.appointmentTime()
        );
//        email for patient and doctor both
        emailService.sendSimpleEmail(
                event.userEmail(),
                "Appointment Booked Successfully",
                userEmailBody
        );

        String doctorMessage = String.format(
                "Patient %s has Booked the appointment scheduled for %s. date and %s time.",
                event.patientName(),
                event.appointmentDate(),
                event.appointmentTime());

        emailService.sendSimpleEmail(
                event.doctorEmail(),
                "New Appointment Booked",
                doctorMessage
        );

        notificationService.sendNotification(
                event.doctorEmail(),
                "New Appointment Booked",
                doctorMessage,
                AppointmentStatus.BOOKED,
                event.appointmentId().toString()
        );
    }
}
