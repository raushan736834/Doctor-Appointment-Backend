package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorInfoResponse;
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

    public ApiResponse<?> getCurrentUser(String email) {
        Users user = repo.findByEmail(email);
        if (user == null) throw new RuntimeException("User Not Found");

        String fullName = user.getFirstName() + " " + user.getLastName();
        String accessToken = jwtService.generateAccessToken(email); // Generate token

        if (user.getRoles().contains(Role.DOCTOR)) {
            Doctor doctor = doctorRepo.findByEmail(email);
            if (doctor != null) {
                String doctorId = doctor.getDoctorId();
                String accountStatus = doctor.getAccountStatus().name();
                int consultationFees = doctor.getProfessional().getConsultationFees();
                DoctorInfoResponse doctorInfoResponse = new DoctorInfoResponse(
                        user.getEmail(),
                        fullName,
                        user.getRoles(),
                        doctorId,
                        accountStatus,
                        accessToken,
                        consultationFees
                );
                return ApiResponse.success(doctorInfoResponse,"Data Fetched",200);
            }
        }

        UserInfoResponse userInfoResponse =  new UserInfoResponse(
                user.getEmail(),
                fullName,
                user.getRoles(),
                accessToken
        );
        return ApiResponse.success(userInfoResponse,"Data Fetched",200);
    }


    @Transactional
    public void register(Users user) {
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
    }

    public boolean userExistence(String email) {
        Users user = repo.findByEmail(email);
        return user != null;
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

    public ResponseEntity<ApiResponse<?>> validateOtp(Users user) {
        Users usersOptional = repo.findByEmail(user.getEmail());
        if (usersOptional != null){
            if (user.getOtp() == usersOptional.getOtp()){
                return new ResponseEntity<>(ApiResponse.success(null,"OTP Matched",200), HttpStatus.OK);
            } else
                return new ResponseEntity<>(ApiResponse.error("Wrong Otp",417),HttpStatus.EXPECTATION_FAILED);
        } else {
            return new ResponseEntity<>(ApiResponse.error("Unauthorised user",401),HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<ApiResponse<?>> updatePassword(Users user) {
        Users usersOptional = repo.findByEmail(user.getEmail());
        if (usersOptional != null){
            String userFirstName = usersOptional.getFirstName();
            String userLastName = usersOptional.getLastName();

            user.setFirstName(userFirstName);
            user.setLastName(userLastName);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setOtp(0);
            repo.save(user);
            return new ResponseEntity<>(ApiResponse.success(null,"Password Updated",200),HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

//    public ResponseEntity<ApiResponse<?>> getUserById(String email) {
//        Users userOptional = repo.findByEmail(email);
//        if (userOptional != null){
//            String firstName = userOptional.getFirstName();
//            String lastName = userOptional.getLastName();
//
//            String fullName = firstName +" "+ lastName;
//            return new ResponseEntity<>(ApiResponse.success(fullName,),HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
    public void changePassword(String email, String oldPassword, String newPassword) {
        Users user = repo.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException("Unauthorised User");

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(encoder.encode(newPassword));
        repo.save(user);
    }
}
