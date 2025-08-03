package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentBookingRepo appointmentRepo;
    private final MailService emailService;
    private final DoctorProfileRepo doctorProfileRepo;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final SocketIOService socketIOService;

//    public AppointmentBooking bookAppointment(AppointmentBooking booking) {
//        AppointmentBooking savedBooking = appointmentRepo.save(booking);
//        String emailBody = String.format(
//                "Dear %s,\n\nYour appointment with Dr. %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
//                booking.getFullName(),
//                doctorData.getDoctorName(),
//                doctorData.getSpecialization(),
//                booking.getDate(),
//                booking.getTime()
//        );
//         Send confirmation email
//        System.out.println(booking.getEmail());
//        emailService.sendSimpleEmail(
//                booking.getEmail(),
//                "Appointment Confirmation",
//                emailBody
//        );
//        emailService.sendSimpleEmail(
//                booking.getPatientEmail(),
//                "Appointment Confirmation",
//                emailBody
//        );
//        return savedBooking;
//    }

    public AppointmentBooking bookAppointment(AppointmentBooking booking, Payment payment) {
        if (booking.getDoctor() == null || booking.getDoctor().getId() == null) {
            throw new IllegalArgumentException("Doctor ID is required for booking.");
        }

        String doctorId = booking.getDoctor().getId();
        DoctorProfile doctor = doctorProfileRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        booking.setDoctor(doctor);

        if (payment != null) {
            payment.setAppointmentBooking(booking);
            booking.setPayment(payment);
        }

        String emailBody = String.format(
                "Dear %s,\n\nYour appointment with %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
                booking.getFullName(),
                doctor.getDoctorName(),
                doctor.getSpecialization(),
                booking.getDate(),
                booking.getTime()
        );
//        Send confirmation email
        System.out.println(booking.getEmail());
        emailService.sendSimpleEmail(
                booking.getEmail(),
                "Appointment Confirmation",
                emailBody
        );
        emailService.sendSimpleEmail(
                booking.getPatientEmail(),
                "Appointment Confirmation",
                emailBody
        );

        return appointmentRepo.save(booking);
    }


    public List<AppointmentBooking> getAppointmentsByEmail(String email) {
        return appointmentRepo.findByEmail(email);
    }

    public List<String> getBookedSlots(String doctorId, String appointmentDate) {
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED, AppointmentStatus.COMPLETED);
        List<AppointmentBooking> bookings = appointmentRepo.findByDoctorIdAndDateAndStatusIn(doctorId, appointmentDate,statuses);
        return bookings.stream().map(AppointmentBooking::getTime).toList();
    }

    @Transactional
    public ResponseEntity<?> cancelAppointment(AppointmentBooking booking) {
        try {
            AppointmentBooking existingBooking = appointmentRepo.findById(booking.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: "
                            + booking.getAppointmentId()));

            existingBooking.setStatus(AppointmentStatus.CANCELLED);
            existingBooking.setReason(booking.getReason());
            existingBooking.setCancelledBy(booking.getCancelledBy());

            notificationService.sendAppointmentCancelledNotification(
                    existingBooking.getEmail(),
                    existingBooking.getAppointmentId(),
                    existingBooking.getDoctor().getDoctorName(),
                    existingBooking.getDate()
            );
            appointmentRepo.save(existingBooking);
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while cancelling the appointment: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AppointmentBooking> getActiveOrFutureAppointments(String email) {
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.RESCHEDULED
        );

        String today = LocalDate.now().toString();

        return appointmentRepo.findActiveOrFutureAppointmentsByEmail(email, today, activeStatuses);
    }

    @Transactional
    public ResponseEntity<?> rescheduleAppointment(AppointmentBooking booking) {
        try {
            AppointmentBooking existingBooking = appointmentRepo.findById(booking.getAppointmentId())
                    .orElseThrow(()-> new IllegalArgumentException("No Appointment Found"));
            System.out.println(booking.getAppointmentId());
            String oldDate = existingBooking.getDate();

            existingBooking.setDate(booking.getDate());
            existingBooking.setTime(booking.getTime());
            existingBooking.setStatus(AppointmentStatus.RESCHEDULED);
            appointmentRepo.save(existingBooking);
            notificationService.sendAppointmentRescheduledNotification(
                    existingBooking.getEmail(),
                    existingBooking.getAppointmentId(),
                    existingBooking.getDoctor().getDoctorName(),
                    oldDate,
                    booking.getDate()
            );
            return ResponseEntity.ok("Appointment Reschedule Successfully");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>("An error occurred while Rescheduling the appointment: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long doctorAllAppointment(String doctorId) {
        return appointmentRepo.countByDoctorId(doctorId);
    }

    public Page<AppointmentBooking> doctorAppointmentForToday(String doctorId, int page,
                                                              int size, List<AppointmentStatus> statuses) {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return appointmentRepo.findByDoctorIdAndDateAndStatusIn(doctorId, todayDate, statuses, pageable);
    }

    @Transactional
    public void cancelAppointmentByDoctor(String appointmentId, String cancellationReason) {
        AppointmentBooking appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Update appointment status with reason
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setReason(cancellationReason);
        appointment.setCancelledBy("DOCTOR");
        appointmentRepo.save(appointment);

        // Send enhanced notification to patient with reason
        String title = "Appointment Cancelled by Doctor";
        String message = String.format("Your appointment with %s scheduled for %s has been cancelled. Reason: %s",
                appointment.getDoctor().getDoctorName(),
                appointment.getDate(),
                cancellationReason);

        Notification patientNotification = new Notification(
                appointment.getEmail(),
                title,
                message,
                AppointmentStatus.CANCELLED,
                appointmentId
        );

        notificationRepository.save(patientNotification);
        socketIOService.sendNotificationToUser(appointment.getEmail(), patientNotification);
    }

    // Method for patient to cancel appointment
    @Transactional
    public void cancelAppointmentByPatient(String  appointmentId) {
        AppointmentBooking appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        System.out.println(appointment.getDoctor());
        // Update appointment status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy("PATIENT");
        appointmentRepo.save(appointment);

        // Send notification to doctor
        String doctorMessage = String.format("Patient %s has cancelled the appointment scheduled for %s.",
                appointment.getFullName(),
                appointment.getDate());

        Notification doctorNotification = new Notification(
                appointment.getDoctor().getEmail(),
                "Appointment Cancelled by Patient",
                doctorMessage,
                AppointmentStatus.CANCELLED,
                appointmentId
        );

        notificationRepository.save(doctorNotification);
        socketIOService.sendNotificationToUser(appointment.getDoctor().getEmail(), doctorNotification);
    }
}