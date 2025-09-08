package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class DoctorProfileController {
    @Autowired
    private DoctorProfileService doctorService;

    @GetMapping("/{email}")
    public ResponseEntity<?> fetchDoctorProfile(@PathVariable String email) {
        System.out.println(email);
        DoctorProfile doctors = doctorService.getDoctorData(email);
        if (doctors == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @PutMapping("/addDoctor")
    public ResponseEntity<DoctorProfile> addDoctor(@RequestBody DoctorProfile doctor) {
        DoctorProfile saved = doctorService.saveDoctor(doctor);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getAllDoctor")
    public ResponseEntity<List<DoctorProfile>> getAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
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

    @PostMapping("/saveAllSpecialist")
    public ResponseEntity<?> saveSpecialist(@RequestBody List<Specialist> specialists){
        try{
            List<Specialist> savedSpecialist = doctorService.saveAllSpecialist(specialists);
            if (savedSpecialist != null){
                return new ResponseEntity<>(HttpStatus.CREATED);
            }else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allSpecialist")
    public ResponseEntity<List<String>> getAllSpecialization() {
        List<String> doctors = doctorService.getAllSpecialization();
        return ResponseEntity.ok(doctors);
    }
}

