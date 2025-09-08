package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.ContactUs;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import com.harsh.AppointDoctor.Services.ContactUsService;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/public/")
public class PublicController {
    @Autowired
    private AppointmentBookingService appointmentService;

    @Autowired
    private DoctorProfileService doctorService;

    @Autowired
    private ContactUsService contactUsService;

    @Autowired
    private SpecialistRepo repo;

    @GetMapping("/getSpecialist")
    public ResponseEntity<?> getAllSpecialist(){
        List<Specialist> data = repo.findAllByOrderBySpecialistAsc();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorProfile>> getDoctorByKeyword(@RequestParam String keyword){
        List<DoctorProfile> doctors = doctorService.searchDoctors(keyword);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/searchByCityAndSpecialist")
    public ResponseEntity<?> getDoctorByCityAndSpecialist(@RequestParam String city, @RequestParam String specialist){
        try {
            List<DoctorProfile> doctors = doctorService.searchDoctorsByCityAndSpecialist(city, specialist);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/booked-slots")
    public ResponseEntity<List<String>> getBookedSlots(@RequestBody AppointmentBooking booking) {
        List<String> bookedSlots = appointmentService.getBookedSlots(booking.getDoctor().getId(),
                booking.getDate());
        return ResponseEntity.ok(bookedSlots);
    }

    @PostMapping("/contact-us")
    public ResponseEntity<?> contactUs(@RequestBody ContactUs details){
        try{
            details = contactUsService.contactUs(details);
            if (details != null){
                return new ResponseEntity<>("Message Received",HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/getDoctor")
    public ResponseEntity<?> getDoctorBySpecializationAndId(@RequestBody DoctorProfile doctor){
        DoctorProfile doctorProfile = doctorService.getDoctorBySpecializationAndId(doctor);
        if (doctorProfile != null) {
            return new ResponseEntity<>(doctorProfile, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<?> getCities(){
        try {
            List<String> cities = doctorService.getDistinctCities();
            return new ResponseEntity<>(cities,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
