package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorProfileController {
    @Autowired
    private DoctorProfileService doctorService;

    @PostMapping("/")
    public ResponseEntity<DoctorProfile> addDoctor(@RequestBody DoctorProfile doctor) {
        DoctorProfile saved = doctorService.saveDoctor(doctor);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getAllDoctor")
    public ResponseEntity<List<DoctorProfile>> getAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorProfile>> getDoctorByKeyword(@RequestParam String keyword){
        System.out.println("searching with "+keyword);
        List<DoctorProfile> doctors = doctorService.searchDoctors(keyword);
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/saveAll")
    public ResponseEntity<?> saveDoctors(@RequestBody List<DoctorProfile> doctors) {
        try {
            List<DoctorProfile> savedDoctors = doctorService.saveAllDoctors(doctors);
            return new ResponseEntity<>(savedDoctors, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/getDoctor")
    public ResponseEntity<?> getDoctorBySpecializationAndId(@RequestBody DoctorProfile doctor){
        System.out.println(doctor);
        DoctorProfile doctorProfile = doctorService.getDoctorBySpecializationAndId(doctor);
        System.out.println(doctorProfile);
        if (doctorProfile != null) {
            return new ResponseEntity<>(doctorProfile, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
        }
    }

}

