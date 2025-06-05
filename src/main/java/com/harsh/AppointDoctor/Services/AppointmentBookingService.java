package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentBookingService {

    @Autowired
    private AppointmentBookingRepo appointmentRepo;

    @Autowired
    private MailService emailService;

    @Autowired
    private DoctorProfileRepo doctorProfileRepo;


//    public AppointmentBooking bookAppointment(AppointmentBooking booking) {
//        AppointmentBooking savedBooking = appointmentRepo.save(booking);
////        String emailBody = String.format(
////                "Dear %s,\n\nYour appointment with Dr. %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
////                booking.getFullName(),
//////                doctorData.getDoctorName(),
//////                doctorData.getSpecialization(),
////                booking.getDate(),
////                booking.getTime()
////        );
//        // Send confirmation email
////        System.out.println(booking.getEmail());
////        emailService.sendSimpleEmail(
////                booking.getEmail(),
////                "Appointment Confirmation",
////                emailBody
////        );
////        emailService.sendSimpleEmail(
////                booking.getPatientEmail(),
////                "Appointment Confirmation",
////                emailBody
////        );
//        return savedBooking;
//    }


    public AppointmentBooking bookAppointment(AppointmentBooking booking) {
        if (booking.getDoctor() == null || booking.getDoctor().getId() == null) {
            throw new IllegalArgumentException("Doctor ID is required for booking.");
        }
        String doctorId = booking.getDoctor().getId();
        DoctorProfile doctor = doctorProfileRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        booking.setDoctor(doctor);
        return appointmentRepo.save(booking);
    }


    public List<AppointmentBooking> getAppointmentsByEmail(String email) {
        return appointmentRepo.findByEmail(email);
    }

    public List<String> getBookedSlots(String doctorId, String appointmentDate) {
        List<AppointmentBooking> bookings = appointmentRepo.findByDoctorIdAndDate(doctorId, appointmentDate);
        return bookings.stream().map(AppointmentBooking::getTime).toList();
    }

    public ResponseEntity<?> cancelAppointment(AppointmentBooking booking) {
        try {
            AppointmentBooking existingBooking = appointmentRepo.findById(booking.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + booking.getAppointmentId()));

            existingBooking.setStatus(booking.isStatus());
            existingBooking.setReason(booking.getReason());
            existingBooking.setCancelledBy(booking.getCancelledBy());

            appointmentRepo.save(existingBooking);

            return ResponseEntity.ok("Appointment cancelled successfully.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while cancelling the appointment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AppointmentBooking> getActiveOrFutureAppointments(String email) {
        String today = java.time.LocalDate.now().toString(); // "yyyy-MM-dd"
        System.out.println(today);
        List<AppointmentBooking> bookingList = appointmentRepo.findActiveOrFutureAppointmentsByEmail(email, today);
        System.out.println(bookingList);
        return bookingList;
    }
}