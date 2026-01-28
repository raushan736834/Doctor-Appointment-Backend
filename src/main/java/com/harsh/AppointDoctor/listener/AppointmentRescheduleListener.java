package com.harsh.AppointDoctor.listener;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.StompNotificationService;
import com.harsh.AppointDoctor.events.AppointmentCancelledEvent;
import com.harsh.AppointDoctor.events.AppointmentRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final NotificationRepository notificationRepository;
    private final StompNotificationService stompNotificationService;
    private final MailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentRescheduled(AppointmentRescheduledEvent event) {

        if(event.rescheduledBy().equals("DOCTOR")) {
            sendNotification(event.userEmail(),"Appointment Rescheduled",
                    "Appointment with " + event.doctorName() + " has been rescheduled from "
                            + event.oldDate() + " " + event.oldTime() + " to "
                            + event.newDate() + " " + event.newTime(),
                    AppointmentStatus.RESCHEDULED,
                    event.appointmentId().toString()
            );
        } else {

            sendNotification(event.doctorEmail(),"Appointment Rescheduled",
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
    public void sendNotification(String email, String title, String message, AppointmentStatus status, String appointmentId) {
        if (email != null) {
            Notification notification = new Notification(email, title, message, status, appointmentId);
            notificationRepository.save(notification);
            stompNotificationService.sendNotificationToUser(email, notification);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancellation(AppointmentCancelledEvent event) {

        // ================= DOCTOR =================
        if ("PATIENT".equalsIgnoreCase(event.cancelledBy())) {

            String doctorMessage = String.format(
                    "Patient %s cancelled the appointment scheduled for %s",
                    event.patientName(),
                    event.appointmentDate()
            );

            sendNotification(
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

            sendNotification(
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

