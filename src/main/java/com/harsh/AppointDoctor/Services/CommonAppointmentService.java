package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.AppointmentDTO;
import com.harsh.AppointDoctor.DTOs.RescheduleAppointmentReqDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Enums.SlotStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.AppointmentSlot;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Models.UserPrincipal;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.events.AppointmentCancelledEvent;
import com.harsh.AppointDoctor.events.AppointmentRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
public class CommonAppointmentService {

    private final AppointmentBookingRepo appointmentRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService paymentService;
    private final AppointmentSlotRepo slotRepo;
    private final SlotLockService lockService;

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

    @Transactional
    public void rescheduleAppointment(
            RescheduleAppointmentReqDTO req,
            Authentication authentication
    ) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String userEmail = principal.getUsername();

        AppointmentBooking booking = appointmentRepo.findById(req.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (principal.isDoctor()) {
            // Doctor can reschedule ONLY their own appointments
            if (!booking.getSlot().getDoctor().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You cannot reschedule this appointment");
            }
        } else if (principal.isUser()) {
            // Patient can reschedule ONLY their own appointment
            if (!booking.getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You cannot reschedule this appointment");
            }
        } else {
            throw new AccessDeniedException("Unauthorized role");
        }

        AppointmentSlot oldSlot = booking.getSlot();

        AppointmentSlot newSlot = slotRepo.findById(req.getNewSlotId())
                .orElseThrow(() -> new IllegalArgumentException("New Slot not found"));

        if (oldSlot.getSlotId().equals(newSlot.getSlotId())) {
            throw new IllegalArgumentException("Cannot reschedule to same slot");
        }

        // Prevent double booking
        if (newSlot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalStateException("Selected slot already booked");
        }

        //  Redis lock verification
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

        // Release old slot
        oldSlot.setStatus(SlotStatus.AVAILABLE);

        // Book new slot
        newSlot.setStatus(SlotStatus.BOOKED);

        booking.setSlot(newSlot);
        booking.setStatus(AppointmentStatus.RESCHEDULED);

        appointmentRepo.save(booking);

        // Release Redis lock
        lockService.releaseLock(newSlot, userEmail);

        // Publish domain event (AFTER COMMIT)
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
}
