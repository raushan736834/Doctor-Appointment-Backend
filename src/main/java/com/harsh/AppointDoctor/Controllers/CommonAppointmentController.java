package com.harsh.AppointDoctor.Controllers;


import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.AppointmentDTO;
import com.harsh.AppointDoctor.DTOs.RescheduleAppointmentReqDTO;
import com.harsh.AppointDoctor.Services.CommonAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/appointment/common")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class CommonAppointmentController {

    private final CommonAppointmentService appointmentService;

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
        appointmentService.rescheduleAppointment(req, authentication);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Appointment Rescheduled Successfully", 200)
        );
    }

}
