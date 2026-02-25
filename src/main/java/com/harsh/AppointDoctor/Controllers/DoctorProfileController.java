package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileService doctorService;

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<?>> fetchDoctorProfile(@PathVariable String email) {
        DoctorProfile doctors = doctorService.getDoctorData(email);
        if (doctors == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ApiResponse.success(doctors,"",200), HttpStatus.OK);
    }

    @PutMapping("/addDoctor")
    public ResponseEntity<ApiResponse<DoctorProfile>> addDoctor(@RequestBody DoctorProfile doctor) {
        DoctorProfile saved = doctorService.saveDoctor(doctor);
        return ResponseEntity.ok(ApiResponse.success(saved,"",200));
    }

    @GetMapping("/getAllDoctor")
    public ResponseEntity<ApiResponse<List<DoctorProfile>>> getAll() {
        List<DoctorProfile> list = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(list,"",200));
    }

    @PostMapping("/saveAll")
    public ResponseEntity<ApiResponse<?>> saveDoctors(@RequestBody List<DoctorProfile> doctors) {
        try {
            List<DoctorProfile> savedDoctors = doctorService.saveAllDoctors(doctors);
            return new ResponseEntity<>(ApiResponse.success(savedDoctors,"",201), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),500), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/saveAllSpecialist")
    public ResponseEntity<ApiResponse<?>> saveSpecialist(@RequestBody List<Specialist> specialists){
        try{
            List<Specialist> savedSpecialist = doctorService.saveAllSpecialist(specialists);
            if (savedSpecialist != null){
                return new ResponseEntity<>(HttpStatus.CREATED);
            }else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),500),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allSpecialist")
    public ResponseEntity<ApiResponse<List<String>>> getAllSpecialization() {
        List<String> doctors = doctorService.getAllSpecialization();
        return ResponseEntity.ok(ApiResponse.success(doctors,"",200));
    }
}

