package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.Doctors;
import com.harsh.AppointDoctor.Services.DoctorServices;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Utility.ErrorResponse;
import com.harsh.AppointDoctor.Utility.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class DoctorController {

    @Autowired
    public DoctorServices service;

    @Autowired
    private MailService mailService;

//    @PutMapping("")
}
