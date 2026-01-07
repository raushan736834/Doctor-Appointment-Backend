package com.harsh.AppointDoctor.Controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorSearchDTO;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.ContactUs;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import com.harsh.AppointDoctor.Services.ContactUsService;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import com.harsh.AppointDoctor.Services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/public/")
public class PublicController {

    private final AppointmentBookingService appointmentService;
    private final RedisService redisService;
    private final DoctorProfileService doctorService;
    private final ContactUsService contactUsService;
    private final SpecialistRepo repo;

    @GetMapping("/getSpecialist")
    public ResponseEntity<?> getAllSpecialist() {
        List<Specialist> specialists =
                redisService.get("all_specialist", new TypeReference<List<Specialist>>() {});

        if (specialists != null) {
            return ResponseEntity.ok(specialists);
        }

        List<Specialist> data = repo.findAllByOrderBySpecialistAsc();
        redisService.set("all_specialist", data, 300L);
        return ResponseEntity.ok(data);
    }


//    @GetMapping("/search")
//    public ResponseEntity<List<DoctorProfile>> getDoctorByKeyword(@RequestParam String keyword){
//        List<DoctorProfile> doctors = doctorService.searchDoctors(keyword);
//        return ResponseEntity.ok(doctors);
//    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> getDoctorDetails(@RequestParam String keyword){
        List<Doctor> doctors = null;
        try {
            doctors = doctorService.searchDoctorDetails(keyword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(ApiResponse.success(doctors, "Doctors fetched successfully", 200), HttpStatus.OK);
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
        List<String> bookedSlots = appointmentService.getBookedSlots(booking.getDoctor().getDoctorId(),
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

//    @PostMapping("/getDoctor")
//    public ResponseEntity<?> getDoctorBySpecializationAndId(@RequestBody DoctorProfile doctor){
//        DoctorProfile doctorProfile = doctorService.getDoctorBySpecializationAndId(doctor);
//        if (doctorProfile != null) {
//            return new ResponseEntity<>(doctorProfile, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
//        }
//    }

    @PostMapping("/getDoctor")
    public ResponseEntity<?> getDoctorDetails(@RequestBody DoctorSearchDTO doctor){
        Doctor doctorDetails = doctorService.getDoctorById(doctor.getDoctorId());
        if (doctorDetails != null) {
            return new ResponseEntity<>(doctorDetails, HttpStatus.OK);
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
