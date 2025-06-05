package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/appointment")
@RestController
@CrossOrigin
public class AppointmentBookingController {

    @Autowired
    AppointmentBookingService appointmentService;

    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentBooking booking) {
        try {
            AppointmentBooking savedBooking = appointmentService.bookAppointment(booking);
            return ResponseEntity.ok("Booking successful: " + savedBooking);
        } catch (Exception e) {
            if (e.getMessage().contains("Error while booking")) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Booking failed: " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
        }
    }


    @GetMapping("/allBooking/{email}")
    public ResponseEntity<List<AppointmentBooking>> getUserAppointments(@PathVariable String email) {
        List<AppointmentBooking> appointments = appointmentService.getAppointmentsByEmail(email);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }


    @GetMapping("/booking/{email}")
    public ResponseEntity<List<AppointmentBooking>> getActiveOrFutureAppointments(@PathVariable String email) {
        List<AppointmentBooking> appointments = appointmentService.getActiveOrFutureAppointments(email);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }


    @PostMapping("/booked-slots")
    public ResponseEntity<List<String>> getBookedSlots(@RequestBody AppointmentBooking booking) {
        System.out.println(booking);
        List<String> bookedSlots = appointmentService.getBookedSlots(booking.getDoctor().getId(),
                booking.getDate());
        return ResponseEntity.ok(bookedSlots);
    }

    @PutMapping("/cancel-appointment")
    public ResponseEntity<?> cancelAppointment(@RequestBody AppointmentBooking booking){
        System.out.println(booking);
        return appointmentService.cancelAppointment(booking);
    }


}
