package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import com.harsh.AppointDoctor.Services.AppointmentBookingService;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/booked-slots")
    public ResponseEntity<List<String>> getBookedSlots(@RequestBody AppointmentBooking booking) {
        List<String> bookedSlots = appointmentService.getBookedSlots(booking.getDoctor().getId(),
                booking.getDate());
        return ResponseEntity.ok(bookedSlots);
    }
}
