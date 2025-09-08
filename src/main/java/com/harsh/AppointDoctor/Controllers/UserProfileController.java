package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Services.DoctorProfileService;
import com.harsh.AppointDoctor.Services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserProfileController {
    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private DoctorProfileService doctorService;

    @PutMapping("update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UsersProfile profile) {
        try {
            ResponseEntity<?> emailValidationResponse = userProfileService.userExistence(profile.getEmail());

            if (emailValidationResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            return userProfileService.updateUserProfile(profile);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("Error updating profile: ", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email){
        return userProfileService.getUserById(email);
    }
}

