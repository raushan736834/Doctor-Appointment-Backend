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

    @PostMapping("/auth.signup.doctor")
    public ResponseEntity<?> register(@RequestBody Doctors doctor) {
        try {
            // Check if the email already exists
            ResponseEntity<?> emailValidationResponse = service.doctorExistence(doctor.getEmail());
            if (emailValidationResponse.getStatusCode() == HttpStatus.OK) {
                return new ResponseEntity<>("Email already in use", HttpStatus.CONFLICT);
            }

            // Add the user
            Doctors newUser = service.register(doctor);
            mailService.sendSimpleEmail(doctor.getEmail(), "Welcome to Appoint Doctor",
                    "Thank you for registering as a Doctor!\nYou can complete your further profile update in your dashboard");
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/auth.login.doctor")
    public ResponseEntity<?> authoriseUser(@RequestBody Doctors loginRequest) {
        try {
            String token = service.verify(loginRequest);  // The `verify` method will either return a token or throw an exception
            if (!token.equals("fail")) {
                return new ResponseEntity<>(new LoginResponse("Login Successful", HttpStatus.OK.value(),
                        token,loginRequest.getEmail()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Invalid Username or Password", HttpStatus.UNAUTHORIZED.value()),
                        HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
