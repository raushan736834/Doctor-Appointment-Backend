package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.AppointmentDTO;
import com.harsh.AppointDoctor.DTOs.RescheduleAppointmentReqDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.Notification;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentBookingRepo appointmentRepo;
    private final MailService emailService;
    private final DoctorRepo doctorRepo;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final StompNotificationService stompNotificationService;
    private final PaymentService paymentService;

    // public AppointmentBooking bookAppointment(AppointmentBooking booking) {
    // AppointmentBooking savedBooking = appointmentRepo.save(booking);
    // String emailBody = String.format(
    // "Dear %s,\n\nYour appointment with Dr. %s (%s) on %s at %s has been
    // successfully booked.\n\nThank you!",
    // booking.getFullName(),
    // doctorData.getDoctorName(),
    // doctorData.getSpecialization(),
    // booking.getDate(),
    // booking.getTime()
    // );
    // Send confirmation email
    // System.out.println(booking.getEmail());
    // emailService.sendSimpleEmail(
    // booking.getEmail(),
    // "Appointment Confirmation",
    // emailBody
    // );
    // emailService.sendSimpleEmail(
    // booking.getPatientEmail(),
    // "Appointment Confirmation",
    // emailBody
    // );
    // return savedBooking;
    // }

    @Transactional
    public AppointmentBooking bookAppointment(AppointmentBooking booking, Payment payment) {
        if (booking.getDoctor() == null || booking.getDoctor().getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID is required for booking.");
        }

        String doctorId = booking.getDoctor().getDoctorId();
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        booking.setDoctor(doctor);

        if (payment != null) {
            payment.setAppointmentBooking(booking);
            booking.setPayment(payment);
        }

        String emailBody = String.format(
                "Dear %s,\n\nYour appointment with %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
                booking.getFullName(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                doctor.getProfessional().getSpecialization(),
                booking.getDate(),
                booking.getTime());

        // Send confirmation email
        emailService.sendSimpleEmail(
                booking.getEmail(),
                "Appointment Confirmation",
                emailBody);
        emailService.sendSimpleEmail(
                booking.getPatientEmail(),
                "Appointment Confirmation",
                emailBody);

//        send notification to doctor
        String messageForDoctor = String.format("Patient %s has Booked the appointment scheduled for %s.",
                booking.getFullName(),
                booking.getDate());

        sendNotification(booking.getDoctor().getEmail(),
                "Appointment Booked by Patient",
                messageForDoctor,
                AppointmentStatus.BOOKED,
                booking.getAppointmentId());

        return appointmentRepo.save(booking);
    }

    public List<AppointmentBooking> getAppointmentsByEmail(String email) {
        return appointmentRepo.findByEmail(email);
    }

    public List<String> getBookedSlots(String doctorId, String appointmentDate) {
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED,
                AppointmentStatus.COMPLETED);
        List<AppointmentBooking> bookings = appointmentRepo.findByDoctor_DoctorIdAndDateAndStatusIn(doctorId,
                appointmentDate, statuses);
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
                    existingBooking.getDoctor().getFirstName() + " " + existingBooking.getDoctor().getLastName(),
                    existingBooking.getDate());
            appointmentRepo.save(existingBooking);
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while cancelling the appointment: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AppointmentBooking> getActiveOrFutureAppointments(String email) {
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.RESCHEDULED);

        String today = LocalDate.now().toString();

        return appointmentRepo.findActiveOrFutureAppointmentsByEmail(email, today, activeStatuses);
    }

    @Transactional
    public ResponseEntity<?> rescheduleAppointment(RescheduleAppointmentReqDTO booking) {
        try {
            AppointmentBooking existingBooking = appointmentRepo.findById(booking.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("No Appointment Found"));
            System.out.println(booking.getAppointmentId());
            String oldDate = existingBooking.getDate();

            existingBooking.setDate(booking.getNewDate());
            existingBooking.setTime(booking.getNewTimeSlot());
            existingBooking.setStatus(AppointmentStatus.RESCHEDULED);
            appointmentRepo.save(existingBooking);
            notificationService.sendAppointmentRescheduledNotification(
                    existingBooking.getEmail(),
                    existingBooking.getAppointmentId(),
                    existingBooking.getDoctor().getFirstName() + " " + existingBooking.getDoctor().getLastName(),
                    oldDate,
                    booking.getNewDate());
            return ResponseEntity.ok("Appointment Reschedule Successfully");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while Rescheduling the appointment: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long doctorAllAppointment(String doctorId) {
        return appointmentRepo.countByDoctor_DoctorId(doctorId);
    }

    public Page<AppointmentBooking> doctorAppointmentForToday(String doctorId, int page,
            int size, List<AppointmentStatus> statuses) {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return appointmentRepo.findByDoctor_DoctorIdAndDateAndStatusIn(doctorId, todayDate, statuses, pageable);
    }

    @Transactional
    public ResponseEntity<ApiResponse<?>> cancelAppointment(AppointmentDTO dto) {

        AppointmentBooking appointment = appointmentRepo.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Already cancelled");
        }

        appointment.setCancelledBy(dto.getCancelledBy());
        appointment.setReason(dto.getReason());
        appointmentRepo.save(appointment);

        Payment payment = appointment.getPayment();

        // ✅ CASE 1: CASH
        if (!"ONLINE".equalsIgnoreCase(appointment.getSelectedPayment())) {
            cancelAppointmentOnly(appointment, dto.getCancelledBy(), dto.getReason());
            return ResponseEntity.ok(ApiResponse.success(null, "APPOINTMENT CANCELLED", 200));
        }

        // ✅ CASE 2: ONLINE → DO REFUND FIRST
        boolean refundResult = paymentService.initiateRefund(payment);

        if (!refundResult) {
            throw new RuntimeException("Refund failed. Appointment not cancelled.");
        }

        // ✅ Now safe to cancel
        try {
            cancelAppointmentOnly(appointment, dto.getCancelledBy(), dto.getReason());

            return ResponseEntity.ok(ApiResponse.success(Map.of("CANCELLED", "REFUND_INITIATED"),
                    "APPOINTMENT CANCELLED", 200));
        } catch (Exception e) {
            appointment.setStatus(AppointmentStatus.CANCEL_PENDING);
            appointmentRepo.save(appointment);
            return ResponseEntity.ok(ApiResponse.success(Map.of("CANCEL_PENDING", "REFUND_INITIATED"),
                    "APPOINTMENT CANCEL PENDING", 500));
        }
    }

    // Method for patient to cancel appointment
    @Transactional
    public void cancelAppointmentOnly(AppointmentBooking appointment, String cancelledBy, String cancellationReason) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appointment);

        if ("PATIENT".equalsIgnoreCase(cancelledBy)) {
            // Send notification to doctor
            String messageForDoctor = String.format("Patient %s has cancelled the appointment scheduled for %s.",
                    appointment.getFullName(),
                    appointment.getDate());

            sendNotification(appointment.getDoctor().getEmail(),
                    "Appointment Cancelled by Patient",
                    messageForDoctor,
                    AppointmentStatus.CANCELLED,
                    appointment.getAppointmentId());
            // Send notification to patient
        } else if ("DOCTOR".equalsIgnoreCase(cancelledBy)) {

            String title = "Appointment Cancelled by Doctor";

            String messageForPatient = String.format(
                    "Your appointment with %s scheduled for %s has been cancelled. Reason: %s",
                    appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                    appointment.getDate(),
                    cancellationReason);
            sendNotification(appointment.getEmail(),
                    title,
                    messageForPatient,
                    AppointmentStatus.CANCELLED,
                    appointment.getAppointmentId());
        } else {
            throw new IllegalArgumentException("Invalid cancelledBy value. Use 'PATIENT' or 'DOCTOR'.");
        }
    }

    public void sendNotification(String email, String title, String message, AppointmentStatus status,
            String appointmentId) {
        if (email != null) {
            Notification notification = new Notification(email, title, message, status, appointmentId);
            notificationRepository.save(notification);
            stompNotificationService.sendNotificationToUser(email, notification);
        }
    }
}