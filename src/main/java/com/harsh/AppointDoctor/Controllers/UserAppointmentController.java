package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.*;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import com.harsh.AppointDoctor.Services.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/appointment")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class AppointmentBookingController {


    private final AppointmentBookingService appointmentService;

    @PostMapping("/book-appointment")
    public ResponseEntity<ApiResponse<?>> bookAppointment(
            @RequestBody AppointmentBookingRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not authenticated",401));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();
            AppointmentBooking booking =
                    appointmentService.confirmBooking(
                            request.getSlotId(),
                            request.getFormData(),
                            request.getPayment(),
                            userEmail
                    );

            return ResponseEntity.
                    status(HttpStatus.OK).
                    body(ApiResponse.success(booking.getAppointmentId(),"Appointment Booked Successfully",200));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),500),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allBooking/{email}")
    public ResponseEntity<ApiResponse<List<AppointmentBooking>>> getUserAppointments(@PathVariable String email) {
        List<AppointmentBooking> appointments = appointmentService.getAppointmentsByEmail(email);
        return new ResponseEntity<>(ApiResponse.success(appointments,"Appointment Fetched",200), HttpStatus.OK);
    }

    @GetMapping("/bookingUser/{email}")
    public ResponseEntity<ApiResponse<List<UserAppointmentResponse>>> getActiveOrFutureAppointments(@PathVariable String email) {
        List<UserAppointmentResponse> appointments = appointmentService.getActiveOrFutureAppointments(email);
        return new ResponseEntity<>(ApiResponse.success(appointments,"Appointment Fetched",200), HttpStatus.OK);
    }

    @PutMapping("/cancel-appointment")
    public ResponseEntity<ApiResponse<?>> cancelAppointment(
            @RequestBody AppointmentDTO appointmentDTO
    ) {
        appointmentService.cancelAppointment(appointmentDTO);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Appointment Cancelled Successfully", 200)
        );
    }



    @PutMapping("/reschedule-appointment")
    public ResponseEntity<ApiResponse<?>> rescheduleAppointment(
            @RequestBody RescheduleAppointmentReqDTO req,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        appointmentService.rescheduleAppointment(req, userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Appointment Rescheduled Successfully", 200)
        );
    }


    @GetMapping("doctorAllAppointment/{doctorId}")
    public ResponseEntity<ApiResponse<?>> getDoctorAllAppointment(@PathVariable String doctorId){
        try {
            long bookingsCount = appointmentService.doctorAllAppointment(doctorId);
            return ResponseEntity.ok(ApiResponse.success(bookingsCount,"",200));
        } catch (Exception e) {
            log.error("Error fetching doctor appointments", e);
            
            throw new RuntimeException("Error Fetching Appointment");
        }
    }

    @GetMapping("/doctorAppointment/{doctorId}")
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
                appointmentService.getDoctorAppointmentsForToday(
                        doctorId, page, size, statuses, startDate, endDate
                );

        PagedModel<EntityModel<DoctorAppointmentResponseDTO>> model =
                assembler.toModel(pageResult);

        return ResponseEntity.ok(ApiResponse.success(model, "", 200));
    }
}
