package com.harsh.AppointDoctor.Services;


import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SocketIOService socketIOService;

    public void sendAppointmentCancelledNotification(String userEmail, String appointmentId,
                                                     String doctorName, String appointmentDate) {
        String title = "Appointment Cancelled";
        String message = String.format("Your appointment with %s scheduled for %s has been cancelled.",
                doctorName, appointmentDate);

        Notification notification = new Notification(userEmail, title, message,
                AppointmentStatus.CANCELLED, appointmentId);

        // Save to database
        notification = notificationRepository.save(notification);

        // Send real-time notification via Socket.io
        socketIOService.sendNotificationToUser(userEmail, notification);
    }

    public void sendAppointmentCancelledNotificationToDoctor(String doctorEmail, String appointmentId,
                                                             String patientName, String appointmentDate) {
        String title = "Appointment Cancelled";
        String message = String.format("Appointment with patient %s scheduled for %s has been cancelled.",
                patientName, appointmentDate);

        Notification notification = new Notification(doctorEmail, title, message,
                AppointmentStatus.CANCELLED, appointmentId);

        // Save to database
        notification = notificationRepository.save(notification);

        // Send real-time notification via Socket.io
        socketIOService.sendNotificationToUser(doctorEmail, notification);
    }

    public void sendAppointmentRescheduledNotification(String userEmail, String appointmentId,
                                                       String doctorName, String oldDate, String newDate) {
        String title = "Appointment Rescheduled";
        String message = String.format("Your appointment with Dr. %s has been rescheduled from %s to %s.",
                doctorName, oldDate, newDate);

        Notification notification = new Notification(userEmail, title, message,
                AppointmentStatus.RESCHEDULED, appointmentId);

        // Save to database
        notification = notificationRepository.save(notification);

        // Send real-time notification via Socket.io
        socketIOService.sendNotificationToUser(userEmail, notification);
    }

    public void sendAppointmentRescheduledNotificationToDoctor(String doctorEmail, String appointmentId,
                                                               String patientName, String oldDate, String newDate) {
        String title = "Appointment Rescheduled";
        String message = String.format("Appointment with patient %s has been rescheduled from %s to %s.",
                patientName, oldDate, newDate);

        Notification notification = new Notification(doctorEmail, title, message,
                AppointmentStatus.RESCHEDULED, appointmentId);

        // Save to database
        notification = notificationRepository.save(notification);

        // Send real-time notification via Socket.io
        socketIOService.sendNotificationToUser(doctorEmail, notification);
    }

    // Combined method to send cancellation notifications to both parties
    public void sendAppointmentCancelledNotifications(String patientEmail, String doctorEmail,
                                                      String  appointmentId, String doctorName,
                                                      String patientName, String appointmentDate) {
        // Send notification to patient
        sendAppointmentCancelledNotification(patientEmail, appointmentId, doctorName, appointmentDate);

        // Send notification to doctor
        sendAppointmentCancelledNotificationToDoctor(doctorEmail, appointmentId, patientName, appointmentDate);
    }

    // Combined method to send rescheduling notifications to both parties
    public void sendAppointmentRescheduledNotifications(String patientEmail, String doctorEmail,
                                                        String appointmentId, String doctorName,
                                                        String patientName, String oldDate, String newDate) {
        // Send notification to patient
        sendAppointmentRescheduledNotification(patientEmail, appointmentId, doctorName, oldDate, newDate);

        // Send notification to doctor
        sendAppointmentRescheduledNotificationToDoctor(doctorEmail, appointmentId, patientName, oldDate, newDate);
    }

    public List<Notification> getUserNotifications(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public long getUnreadCount(String userEmail) {
        return notificationRepository.countUnreadByUserEmail(userEmail);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        notificationRepository.markAllAsReadByUserEmail(userEmail);
    }
}