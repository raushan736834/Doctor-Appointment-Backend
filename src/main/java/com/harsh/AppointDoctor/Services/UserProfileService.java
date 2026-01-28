package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Repo.UserProfileRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import org.hibernate.metamodel.internal.AbstractPojoInstantiator;
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

    public ResponseEntity<ApiResponse<?>> updateUserProfile(UsersProfile profile) {
        UsersProfile existingProfile = userProfileRepo.findByEmail(profile.getEmail());
        if (existingProfile != null) {
            existingProfile.setFullName(profile.getFullName());
            existingProfile.setPhone(profile.getPhone());
            existingProfile.setGender(profile.getGender());
            existingProfile.setCountry(profile.getCountry());
            existingProfile.setAddress(profile.getAddress());
            existingProfile.setCity(profile.getCity());
            existingProfile.setState(profile.getState());
            existingProfile.setPincode(profile.getPincode());
            UsersProfile updatedProfile = userProfileRepo.save(existingProfile);
            return new ResponseEntity<>(ApiResponse.success(null,"Profile updated successfully",200), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ApiResponse.error("Profile not found",404), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ApiResponse<?>> getUserById(String email) {
        UsersProfile userProfile = userProfileRepo.findByEmail(email);
        Users user = repo.findByEmail(email);        
        if (user != null && userProfile != null){
//            String fullname = userProfile.getFullName();
            return new ResponseEntity<>(ApiResponse.success(userProfile,"",200),HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
