package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Repo.UserProfileRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    
    @Autowired
    UserRepo repo;
    
    @Autowired
    UserProfileRepo userProfileRepo;
    
    public ResponseEntity<?> userExistence(String email) {
        Users user = repo.findByEmail(email);
        UsersProfile userProfile = userProfileRepo.findByEmail(email); 
        if (user != null && userProfile != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateUserProfile(UsersProfile profile) {
        System.out.println(profile);
        UsersProfile existingProfile = userProfileRepo.findByEmail(profile.getEmail());
        System.out.println("fullname "+profile.getFullName());
        if (existingProfile != null) {
            existingProfile.setFullName(profile.getFullName());
            existingProfile.setPhone(profile.getPhone());
            existingProfile.setGender(profile.getGender());
            existingProfile.setCountry(profile.getCountry());
            existingProfile.setAddress(profile.getAddress());
            existingProfile.setCity(profile.getCity());
            existingProfile.setState(profile.getState());
            existingProfile.setPincode(profile.getPincode());
            userProfileRepo.save(existingProfile);
            return new ResponseEntity<>("Profile updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getUserById(String email) {
        UsersProfile userProfile = userProfileRepo.findByEmail(email);
        Users user = repo.findByEmail(email);        
        if (user != null && userProfile != null){
//            String fullname = userProfile.getFullName();
            return new ResponseEntity<>(userProfile,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
