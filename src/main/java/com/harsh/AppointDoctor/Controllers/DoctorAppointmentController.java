package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Services.DoctorAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequestMapping("/appointment/doctor")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class DoctorAppointmentController {

    private final DoctorAppointmentService appointmentService;

    @GetMapping("/appointment-count/{doctorId}")
    public ResponseEntity<ApiResponse<?>> getDoctorAppointmentCount(@PathVariable String doctorId){
        try {
            long bookingsCount = appointmentService.doctorAllAppointment(doctorId);
            return ResponseEntity.ok(ApiResponse.success(bookingsCount,"",200));
        } catch (Exception e) {
            log.error("Error fetching doctor appointments", e);

            throw new RuntimeException("Error Fetching Appointment");
        }
    }

    @GetMapping("/appointments/{doctorId}")
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<DoctorAppointmentResponseDTO>>>>
    getDoctorAppointmentForToday(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "BOOKED") List<AppointmentStatus> statuses,
            Authentication authentication,
            PagedResourcesAssembler<DoctorAppointmentResponseDTO> assembler
    ) {

        // 🔐 SECURITY CHECK
        if (!doctorId.equals(appointmentService.extractDoctorId(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You are not allowed to access this doctor's appointments", 403));
        }

        Page<DoctorAppointmentResponseDTO> pageResult =
                appointmentService.getDoctorAppointments(
                        doctorId, page, size, statuses, startDate, endDate
                );

        PagedModel<EntityModel<DoctorAppointmentResponseDTO>> model =
                assembler.toModel(pageResult);

        return ResponseEntity.ok(ApiResponse.success(model, "", 200));
    }

    @GetMapping("/mark-complete/{appointmentId}")
    public ResponseEntity<ApiResponse<?>> markAppointmentAsComplete(
            @PathVariable UUID appointmentId,
            Authentication authentication
    ) {
        try {
            appointmentService.markAppointmentAsComplete(appointmentId,authentication);
            return ResponseEntity.ok(ApiResponse.success(null, "Appointment marked as complete", 200));
        } catch (Exception e) {
            log.error("Error marking appointment as complete", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
