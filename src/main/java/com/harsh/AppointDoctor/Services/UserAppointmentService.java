package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.UserAppointmentResponse;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Enums.SlotStatus;
import com.harsh.AppointDoctor.Models.*;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.events.AppointmentBookingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAppointmentService {

    private final AppointmentBookingRepo appointmentRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final AppointmentSlotRepo slotRepo;
    private final SlotLockService lockService;

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

    public List<UserAppointmentResponse> getPastAppointments(String email) {
        List<AppointmentStatus> pastStatuses = Arrays.asList(
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED);

        LocalDate today = LocalDate.now();

        return appointmentRepo.findPastAppointmentsByEmail(email, today, pastStatuses);
    }
}