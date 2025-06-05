package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Repo.UserProfileRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import com.harsh.AppointDoctor.Utility.ErrorResponse;
import com.harsh.AppointDoctor.Utility.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private UserProfileRepo profileRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Users register(Users user) {
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRoles() == null) {
            user.setRoles("user");
        }

        Users savedUser = repo.save(user);

        UsersProfile userProfile = new UsersProfile();
        userProfile.setFullName(savedUser.getFirstName() + " " + savedUser.getLastName());
        userProfile.setEmail(savedUser.getEmail());

        profileRepo.save(userProfile);

        return savedUser;
    }

    public ResponseEntity<?> userExistence(String email) {
        Users user = repo.findByEmail(email);
        if (user != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }
    }

//    public ResponseEntity<?> verify(Users loginRequest) {
//        Authentication authentication =
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                loginRequest.getEmail(), loginRequest.getPassword()
//                        )
//                );
//
//        if (authentication.isAuthenticated()) {
//            String token = jwtService.generateToken(loginRequest.getEmail());
//            // Retrieve authenticated user details
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            // Extract roles as a single String
//            String roles = userDetails.getAuthorities().stream()
//                    .map(GrantedAuthority::getAuthority)
//                    .collect(Collectors.joining(","));  // Converts list to comma-separated String
//
//            return new ResponseEntity<>(new LoginResponse("Login Successful", HttpStatus.OK.value(), token,
//                    loginRequest.getEmail(), roles,
//                    loginRequest.getFirstName()+" "+ loginRequest.getLastName()),HttpStatus.B);
//
//        }
//        return null;
//    }

    public ResponseEntity<?> verify(Users loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Generate token
                String token = jwtService.generateToken(userDetails.getUsername());

                // Extract roles
                String roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                // Fetch full name from DB using email
                Users userFromDB = repo.findByEmail(loginRequest.getEmail());
                if(userFromDB == null){
                    return new ResponseEntity<>("Authentication Failed",HttpStatus.UNAUTHORIZED);
                }

                String fullName = userFromDB.getFirstName() + " " + userFromDB.getLastName();

                return new ResponseEntity<>(new LoginResponse("Login Successful", HttpStatus.OK.value(), token,
                        loginRequest.getEmail(), roles, fullName),HttpStatus.OK);
            }
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Internal error during login", e);
        }
        return null;
    }

    public void addOtp(Users user, int otp) {
        Users usersOptional = repo.findByEmail(user.getEmail());
        if (usersOptional != null) {
            String userOldPassword = usersOptional.getPassword();
            String userFirstName = usersOptional.getFirstName();
            String userLastName = usersOptional.getLastName();

            user.setPassword(userOldPassword);
            user.setFirstName(userFirstName);
            user.setLastName(userLastName);
            user.setOtp(otp);
            repo.save(user);
        }
    }

    public ResponseEntity<?> validateOtp(Users user) {
        Users usersOptional = repo.findByEmail(user.getEmail());
        if (usersOptional != null){

            if (user.getOtp() == usersOptional.getOtp()){
                return new ResponseEntity<>("OTP Matched", HttpStatus.OK);
            } else
                return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        } else
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<?> updatePassword(Users user) {
        Users usersOptional = repo.findByEmail(user.getEmail());
        if (usersOptional != null){

            String userFirstName = usersOptional.getFirstName();
            String userLastName = usersOptional.getLastName();

            user.setFirstName(userFirstName);
            user.setLastName(userLastName);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setOtp(0);
            repo.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<String> getUserById(String email) {
        Users userOptional = repo.findByEmail(email);
        if (userOptional != null){
            String firstName = userOptional.getFirstName();
            String lastName = userOptional.getLastName();

            String fullName = firstName +" "+ lastName;
            return new ResponseEntity<>(fullName,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
