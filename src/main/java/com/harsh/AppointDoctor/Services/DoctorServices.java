package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.Doctors;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.DoctorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoctorServices {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    DoctorRepo repo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    public ResponseEntity<?> doctorExistence(String email) {
        Optional<Doctors> doctor = repo.findById(email);
        if (doctor.isPresent()) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }
    }

    public Doctors register(Doctors doctor) {
        doctor.setPassword(encoder.encode(doctor.getPassword()));
        return repo.save(doctor);
    }

    public String verify(Doctors loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

        if (authentication.isAuthenticated()){
            return jwtService.generateToken(loginRequest.getEmail());
        }
        return "fail";
    }
}
