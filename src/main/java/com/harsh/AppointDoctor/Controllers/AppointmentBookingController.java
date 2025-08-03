package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.AppointmentDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.DTOs.AppointmentBookingRequest;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/appointment")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AppointmentBookingController {


    private final AppointmentBookingService appointmentService;

    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentBookingRequest request) {
        AppointmentBooking booking = request.getFormData();
        Payment payment = request.getPayment();

        try {
            AppointmentBooking savedBooking = appointmentService.bookAppointment(booking, payment);
            return ResponseEntity.ok("Booking successful: " + savedBooking.getAppointmentId());
        } catch (Exception e) {
            if (e.getMessage().contains("Error while booking")) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                        .body("Booking failed: " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/allBooking/{email}")
    public ResponseEntity<List<AppointmentBooking>> getUserAppointments(@PathVariable String email) {
        List<AppointmentBooking> appointments = appointmentService.getAppointmentsByEmail(email);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/bookingUser/{email}")
    public ResponseEntity<List<AppointmentBooking>> getActiveOrFutureAppointments(@PathVariable String email) {
        List<AppointmentBooking> appointments = appointmentService.getActiveOrFutureAppointments(email);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @PutMapping("/cancel-appointment")
    public ResponseEntity<String> cancelAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        try {
            if ("DOCTOR".equalsIgnoreCase(appointmentDTO.getDoneBy())) {
                appointmentService.cancelAppointmentByDoctor(appointmentDTO.getAppointmentId(), appointmentDTO.getReason());
            } else if ("PATIENT".equalsIgnoreCase(appointmentDTO.getDoneBy())) {
                appointmentService.cancelAppointmentByPatient(appointmentDTO.getAppointmentId());
            } else {
                return ResponseEntity.badRequest().body("Invalid cancelledBy Value. Use 'PATIENT' or 'DOCTOR'");
            }

            return ResponseEntity.ok("Appointment cancelled successfully and notifications sent.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error cancelling appointment: " + e.getMessage());
        }
    }

    @PutMapping("/reschedule-appointment")
    public ResponseEntity<?> rescheduleAppointment(@RequestBody AppointmentBooking booking){
        return appointmentService.rescheduleAppointment(booking);
    }

    @GetMapping("doctorAllAppointment/{doctorId}")
    public ResponseEntity<?> getDoctorAllAppointment(@PathVariable String doctorId){
        long bookingsCount = appointmentService.doctorAllAppointment(doctorId);
        return ResponseEntity.ok(bookingsCount);
    }

    @GetMapping("/doctorAppointment/{doctorId}")
    public ResponseEntity<PagedModel<EntityModel<AppointmentBooking>>> getDoctorAppointmentForToday(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "BOOKED") List<AppointmentStatus> statuses,
            PagedResourcesAssembler<AppointmentBooking> assembler) {

        Page<AppointmentBooking> bookings = appointmentService.doctorAppointmentForToday(doctorId, page, size, statuses);
        PagedModel<EntityModel<AppointmentBooking>> model = assembler.toModel(bookings);

        return ResponseEntity.ok(model);
    }
}
