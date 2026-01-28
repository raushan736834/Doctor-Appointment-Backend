package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/doctor")
public class SpecialistController {
    @Autowired
    private DoctorProfileService doctorService;

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<?>> fetchDoctorProfile(@PathVariable String email) {
        System.out.println(email);
        DoctorProfile doctors = doctorService.getDoctorData(email);
        if (doctors == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ApiResponse.success(doctors,"",200), HttpStatus.OK);
    }


}
