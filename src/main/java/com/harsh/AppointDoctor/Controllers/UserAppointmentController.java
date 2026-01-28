package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.*;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorAppointmentResponseDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Services.UserAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/appointment/user")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class UserAppointmentController {


    private final UserAppointmentService appointmentService;

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

    @GetMapping("/booking/{email}")
    public ResponseEntity<ApiResponse<List<UserAppointmentResponse>>> getActiveOrFutureAppointments(@PathVariable String email) {
        List<UserAppointmentResponse> appointments = appointmentService.getActiveOrFutureAppointments(email);
        return new ResponseEntity<>(ApiResponse.success(appointments,"Appointment Fetched",200), HttpStatus.OK);
    }

    @GetMapping("/past-booking/{email}")
    public ResponseEntity<ApiResponse<List<UserAppointmentResponse>>> getPastAppointments(
            @PathVariable String email,
            Authentication authentication
    ) {
        // OPTIONAL: security check
        if (!authentication.getName().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", 403));
        }

        List<UserAppointmentResponse> appointments =
                appointmentService.getPastAppointments(email);

        return ResponseEntity.ok(
                ApiResponse.success(appointments, "Appointment Fetched", 200)
        );
    }

}
