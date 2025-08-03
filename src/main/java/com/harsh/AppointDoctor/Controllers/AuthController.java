package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.*;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.RefreshToken;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;
import com.harsh.AppointDoctor.Services.JWTService;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.AuthService;
import com.harsh.AppointDoctor.Services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.harsh.AppointDoctor.Utility.OtpGenerator.generateSixDigitOtp;

@RestController
@CrossOrigin
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final JWTService jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final DoctorProfileRepo doctorProfileRepo;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            ResponseEntity<?> emailValidationResponse = service.userExistence(user.getEmail());
            if (emailValidationResponse.getStatusCode() == HttpStatus.OK) {
                return new ResponseEntity<>("Email already in use", HttpStatus.CONFLICT);
            }
            // Add the user
            Users newUser = service.register(user);
            mailService.sendSimpleEmail(user.getEmail(), "Welcome to Appoint Doctor",
                    "Thank you for registering!");
            return new ResponseEntity<>("Account Created", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResult loginResult = service.verify(loginRequest);

            String accessToken = jwtUtil.generateToken(loginResult.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginResult.getEmail());

            if (Objects.equals(loginResult.getRoles(), "ROLE_USER,ROLE_DOCTOR")){
                DoctorProfile doctorProfile = doctorProfileRepo.findByEmail(loginRequest.getEmail());
                LoginResponse response = new LoginResponse(
                        "Login successful",
                        HttpStatus.OK.value(),
                        accessToken,
                        refreshToken.getToken(),
                        loginResult.getEmail(),
                        loginResult.getRoles(),
                        loginResult.getFullName(),
                        doctorProfile.getId()
                );
                return ResponseEntity.ok(response);

            }
            LoginResponse response = new LoginResponse(
                    "Login successful",
                    HttpStatus.OK.value(),
                    accessToken,
                    refreshToken.getToken(),
                    loginResult.getEmail(),
                    loginResult.getRoles(),
                    loginResult.getFullName()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error during login.");
        }
    }


    @PostMapping("/forget")
    public ResponseEntity<?> forgetPassword(@RequestBody Users user){
        ResponseEntity<?> emailValidationResponse = service.userExistence(user.getEmail());
        if (emailValidationResponse.getStatusCode() == HttpStatus.OK) {
            int otp = generateSixDigitOtp();
            mailService.sendSimpleEmail(user.getEmail(), "Appoint Doctor - Recover Your Account",
                    "Otp for recovering account: "+ otp);
            service.addOtp(user,otp);
            return new ResponseEntity<>(otp, HttpStatus.OK);
        }else {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/forget/verify")
    public ResponseEntity<?> validateOTP(@RequestBody Users user){
        return service.validateOtp(user);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Users user){
        return service.updatePassword(user);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required for logout");
        }
        refreshTokenService.deleteByUsername(email);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(request.getRefreshToken());
        if (refreshTokenOpt.isEmpty() || !refreshTokenService.isValid(refreshTokenOpt.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        String username = refreshTokenOpt.get().getUsername();
        String newAccessToken = jwtUtil.generateToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        try {
            service.changePassword(email, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
