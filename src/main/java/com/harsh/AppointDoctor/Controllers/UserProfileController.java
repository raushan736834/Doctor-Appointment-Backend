package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-profile")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileService profileService;

    @PutMapping("update")
    public ResponseEntity<?> updateProfile(@RequestBody UsersProfile profile) {
        try {
            // Check if the user exists before updating the profile
            ResponseEntity<?> emailValidationResponse = profileService.userExistence(profile.getEmail());

            if (emailValidationResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            // Call service method to update the profile
            return profileService.updateUserProfile(profile);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating profile: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email){
        return profileService.getUserById(email);
    }
}

