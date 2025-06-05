package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpecialistController {
    @Autowired
    private SpecialistRepo repo;

    @GetMapping("/getSpecialist")
    public ResponseEntity<?> getAllSpecialist(){
        List<Specialist> data = repo.findAllByOrderBySpecialistAsc();
        return new ResponseEntity<>(data,HttpStatus.OK);
    }


}
