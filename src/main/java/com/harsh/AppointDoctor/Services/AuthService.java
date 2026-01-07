package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.UserInfoResponse;
import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Role;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Models.UsersProfile;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;
import com.harsh.AppointDoctor.Repo.UserProfileRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private UserProfileRepo profileRepo;

    @Autowired
    DoctorProfileRepo doctorProfileRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserInfoResponse getCurrentUser(String email) {
        Users user = repo.findByEmail(email);
        if (user == null) throw new RuntimeException("User Not Found");


        String doctorId = null;
        String accountStatus = null;

        if (user.getRoles().contains(Role.DOCTOR)) {
            Doctor doctor = doctorRepo.findByEmail(email);
            if (doctor != null) {
                doctorId = doctor.getDoctorId();
                accountStatus = doctor.getAccountStatus().name();
            }
        }

        String fullName = user.getFirstName() + " " + user.getLastName();
        String accessToken = jwtService.generateAccessToken(email); // Generate token

        return new UserInfoResponse(
                user.getEmail(),
                fullName,
                user.getRoles(),
                doctorId,
                accountStatus,
                accessToken // Pass token to response
        );
    }


    @Transactional
    public Users register(Users user) {
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRoles() == null) {
            user.setRoles(List.of(Role.USER));
        } else {
            // Convert each role to uppercase before assigning
            List<Role> uppercaseRoles = user.getRoles().stream()
                    .map(role -> Role.valueOf(role.name().toUpperCase()))
                    .toList();
            user.setRoles(uppercaseRoles);
        }

        Users savedUser = repo.save(user);
        if (new HashSet<>(savedUser.getRoles()).containsAll(List.of(Role.USER,Role.DOCTOR))){

            DoctorProfile doctorProfile = new DoctorProfile();
            doctorProfile.setId(UUID.randomUUID().toString());
            doctorProfile.setEmail(savedUser.getEmail());
            doctorProfile.setDoctorName(savedUser.getFirstName()+" "+savedUser.getLastName());
            doctorProfileRepo.save(doctorProfile);
            Doctor doctor = new Doctor();
            String generatedId = doctor.getDoctorId() == null || doctor.getDoctorId().isEmpty()
                    ? UUID.randomUUID().toString()
                    : doctor.getDoctorId();
            doctor.setDoctorId(generatedId);
            doctor.setEmail(user.getEmail());
            doctor.setFirstName(user.getFirstName());
            doctor.setLastName(user.getLastName());
            doctor.setAccountStatus(AccountStatus.REGISTERED);
            doctorRepo.save(doctor);
        } else {
            UsersProfile userProfile = new UsersProfile();
            userProfile.setFullName(savedUser.getFirstName() + " " + savedUser.getLastName());
            userProfile.setEmail(savedUser.getEmail());
            profileRepo.save(userProfile);
        }
        return savedUser;
    }

    public boolean userExistence(String email) {
        Users user = repo.findByEmail(email);
        return user != null;
    }

//    public LoginResult verify(LoginRequest loginRequest) {
//        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
//            throw new IllegalArgumentException("Email and password are required.");
//        }
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getEmail(),
//                        loginRequest.getPassword()
//                  )
//        );
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));
//
//        Users userFromDB = repo.findByEmail(loginRequest.getEmail());
//        if (userFromDB == null) {
//            throw new UsernameNotFoundException("User not found in database.");
//        }
//
//        String fullName = userFromDB.getFirstName() + " " + userFromDB.getLastName();
//        return new LoginResult(loginRequest.getEmail(), roles, fullName);
//    }

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
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
    public void changePassword(String email, String oldPassword, String newPassword) {
        Users user = repo.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException("User Not Found");

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(encoder.encode(newPassword));
        repo.save(user);
    }
}
