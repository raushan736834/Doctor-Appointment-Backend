package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.UserPrincipal;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.AppointmentSlotRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DoctorAppointmentService {

    private final AppointmentBookingRepo appointmentRepo;
    private final DoctorRepo doctorRepo;

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

    public String extractDoctorId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (!principal.isDoctor()) {
            throw new AccessDeniedException("User is not a doctor");
        }

        return doctorRepo.findByEmail(principal.getUsername())
                .getDoctorId();
    }

    @Transactional
    public void markAppointmentAsComplete(UUID appointmentId, Authentication authentication) {
        AppointmentBooking appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String doctorEmail = principal.getUsername();

        if (!doctorEmail.equals(appointment.getSlot().getDoctor().getEmail())){
            throw new AccessDeniedException("You are not authorized to complete this appointment");
        }

        if(appointment.getStatus() == AppointmentStatus.COMPLETED){
            throw new RuntimeException("Appointment is already completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(appointment);
    }
}
