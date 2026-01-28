package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.AppointmentDTO;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.DTOs.RescheduleAppointmentReqDTO;
import com.harsh.AppointDoctor.DTOs.UserAppointmentResponse;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Enums.SlotStatus;
import com.harsh.AppointDoctor.Models.*;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.NotificationRepository;
import com.harsh.AppointDoctor.events.AppointmentBookingEvent;
import com.harsh.AppointDoctor.events.AppointmentCancelledEvent;
import com.harsh.AppointDoctor.events.AppointmentRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentBookingRepo appointmentRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService paymentService;
    private final AppointmentSlotRepo slotRepo;
    private final SlotLockService lockService;
    private final DoctorRepo doctorRepo;

    @Transactional
    public AppointmentBooking confirmBooking(
            String slotId,
            AppointmentBooking booking,
            Payment payment,
            String userEmail
    ) {
        AppointmentSlot slot = slotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new RuntimeException("Slot already booked");
        }

        boolean locked = lockService.isLockedByUser(
                slot.getDoctor().getDoctorId(),
                slot.getSlotDate(),
                slot.getSlotTime(),
                userEmail
        );

        if (!locked) {
            throw new RuntimeException("Slot lock expired");
        }

        slot.setStatus(SlotStatus.BOOKED);

        booking.setSlot(slot);
        booking.setStatus(AppointmentStatus.BOOKED);
        booking.setEmail(userEmail);

        if (payment != null) {
            payment.setAppointmentBooking(booking);
            booking.setPayment(payment);
        }

        AppointmentBooking savedBooking = appointmentRepo.save(booking);

        eventPublisher.publishEvent(
                new AppointmentBookingEvent(
                        savedBooking.getEmail(),
                        savedBooking.getSlot().getDoctor().getEmail(),
                        savedBooking.getAppointmentId(),
                        savedBooking.getFullName(),
                        savedBooking.getSlot().getDoctor().getFirstName() + " " + savedBooking.getSlot().getDoctor().getLastName(),
                        savedBooking.getSlot().getSlotDate(),
                        savedBooking.getSlot().getSlotTime(),
                        savedBooking.getSlot().getDoctor().getProfessional().getSpecialization()

                )
        );

        lockService.releaseLock(
                slot,
                userEmail
        );

        return savedBooking;
    }

    public List<AppointmentBooking> getAppointmentsByEmail(String email) {
        return appointmentRepo.findByEmail(email);
    }

    public List<LocalTime> getBookedSlots(String doctorId, LocalDate appointmentDate) {
        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.BOOKED,
                AppointmentStatus.RESCHEDULED,
                AppointmentStatus.COMPLETED
        );

        List<AppointmentBooking> bookings =
                appointmentRepo.findBySlot_Doctor_DoctorIdAndSlot_SlotDateAndStatusIn(
                        doctorId,
                        appointmentDate,
                        statuses
                );

        return bookings.stream()
                .map(b -> b.getSlot().getSlotTime())
                .toList();
    }


    public List<UserAppointmentResponse> getActiveOrFutureAppointments(String email) {
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.RESCHEDULED);

        LocalDate today = LocalDate.now();

        return appointmentRepo.findActiveOrFutureAppointmentsByEmail(email, today, activeStatuses);
    }

    @Transactional
    public void rescheduleAppointment(
            RescheduleAppointmentReqDTO req,
            String userEmail
    ) {

        AppointmentBooking booking = appointmentRepo.findById(req.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // 🔐 Ownership check
        if (!booking.getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You cannot reschedule this appointment");
        }

        AppointmentSlot oldSlot = booking.getSlot();

        AppointmentSlot newSlot = slotRepo.findById(req.getNewSlotId())
                .orElseThrow(() -> new IllegalArgumentException("New Slot not found"));

        if (oldSlot.getSlotId().equals(newSlot.getSlotId())) {
            throw new IllegalArgumentException("Cannot reschedule to same slot");
        }

        // ❌ Prevent double booking
        if (newSlot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalStateException("Selected slot already booked");
        }

        // 🔒 Redis lock verification
        boolean locked = lockService.isLockedByUser(
                newSlot.getDoctor().getDoctorId(),
                newSlot.getSlotDate(),
                newSlot.getSlotTime(),
                userEmail
        );

        if (!locked) {
            throw new IllegalStateException("Slot lock expired or owned by another user");
        }

        LocalDate oldDate = oldSlot.getSlotDate();
        LocalTime oldTime = oldSlot.getSlotTime();

        // ✅ Release old slot
        oldSlot.setStatus(SlotStatus.AVAILABLE);

        // ✅ Book new slot
        newSlot.setStatus(SlotStatus.BOOKED);

        booking.setSlot(newSlot);
        booking.setStatus(AppointmentStatus.RESCHEDULED);

        appointmentRepo.save(booking);

        // 🔓 Release Redis lock
        lockService.releaseLock(newSlot, userEmail);

        // 🔥 Publish domain event (AFTER COMMIT)
        eventPublisher.publishEvent(
                new AppointmentRescheduledEvent(
                        booking.getEmail(),
                        newSlot.getDoctor().getEmail(),
                        booking.getAppointmentId(),
                        booking.getFullName(),
                        newSlot.getDoctor().getFirstName() + " " + newSlot.getDoctor().getLastName(),
                        oldDate,
                        newSlot.getSlotDate(),
                        oldTime,
                        newSlot.getSlotTime(),
                        req.getRescheduledBy()
                )
        );
    }


    public long doctorAllAppointment(String doctorId) {
        return appointmentRepo.countBySlot_Doctor_DoctorId(doctorId);
    }

    public Page<DoctorAppointmentResponseDTO> getDoctorAppointments(
            String doctorId,
            int page,
            int size,
            List<AppointmentStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("slot.slotDate"),
                        Sort.Order.asc("slot.slotTime")
                )
        );

        return appointmentRepo.findDoctorAppointmentsBetweenDates(
                doctorId,
                startDate,
                endDate,
                statuses,
                pageable
        );
    }


    @Transactional
    public void cancelAppointment(AppointmentDTO dto) {

        AppointmentBooking appointment = appointmentRepo.findById(dto.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        appointment.setCancelledBy(dto.getCancelledBy());
        appointment.setReason(dto.getReason());

        Payment payment = appointment.getPayment();

        // ================= CASH =================
        if (!"ONLINE".equalsIgnoreCase(appointment.getSelectedPayment())) {
            performCancellation(appointment);
            publishCancelEvent(appointment);
            return;
        }

        // ================= ONLINE =================
        boolean refundStarted = paymentService.initiateRefund(payment);

        if (!refundStarted) {
            throw new IllegalStateException("Refund initiation failed");
        }

        performCancellation(appointment);
        publishCancelEvent(appointment);
    }

    public void performCancellation(AppointmentBooking appointment) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        appointmentRepo.save(appointment);
    }

    private void publishCancelEvent(AppointmentBooking appointment) {

        eventPublisher.publishEvent(
                new AppointmentCancelledEvent(
                        appointment.getAppointmentId(),
                        appointment.getCancelledBy(),
                        appointment.getReason(),
                        appointment.getEmail(),
                        appointment.getSlot().getDoctor().getEmail(),
                        appointment.getFullName(),
                        appointment.getSlot().getDoctor().getFirstName() + " " +
                                appointment.getSlot().getDoctor().getLastName(),
                        appointment.getSlot().getSlotDate()
                )
        );
    }

    public String extractDoctorId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (!principal.isDoctor()) {
            throw new AccessDeniedException("User is not a doctor");
        }

        return doctorRepo.findByEmail(principal.getUsername())
                .getDoctorId();
    }
}