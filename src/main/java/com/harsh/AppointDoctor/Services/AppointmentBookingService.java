package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentBookingService {

    @Autowired
    private AppointmentBookingRepo appointmentRepo;

    @Autowired
    private MailService emailService;

    public AppointmentBooking bookAppointment(AppointmentBooking booking) {
        AppointmentBooking savedBooking = appointmentRepo.save(booking);
        String emailBody = String.format(
                "Dear %s,\n\nYour appointment with Dr. %s (%s) on %s at %s has been successfully booked.\n\nThank you!",
                booking.getFullName(),
                booking.getDoctorName(),
                booking.getSpecialization(),
                booking.getDate(),
                booking.getTime()
        );
        // Send confirmation email
//        System.out.println(booking.getEmail());
        emailService.sendSimpleEmail(
                booking.getEmail(),
                "Appointment Confirmation",
                emailBody
        );
        emailService.sendSimpleEmail(
                booking.getPatientEmail(),
                "Appointment Confirmation",
                emailBody
        );
        System.out.println(booking.getEmail());

        return savedBooking;
    }


    public List<AppointmentBooking> getAppointmentsByEmail(String email) {
        return appointmentRepo.findByEmail(email);
    }

    public List<String> getBookedSlots(int doctorId, String appointmentDate) {
        List<AppointmentBooking> bookings = appointmentRepo.findByDoctorIdAndDate(doctorId, appointmentDate);
        return bookings.stream().map(AppointmentBooking::getTime).toList();
    }
}
