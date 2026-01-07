package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.*;
import com.harsh.AppointDoctor.Enums.Role;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import com.harsh.AppointDoctor.Services.JWTService;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.AuthService;
import com.harsh.AppointDoctor.Services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
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
    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;
    private final AuthenticationManager authManager;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String refreshToken = null;

        // Extract refresh token from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token not found"));
        }

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }

            String email = jwtUtil.extractEmail(refreshToken);

            UserInfoResponse userInfo = service.getCurrentUser(email);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token validation failed"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            boolean emailValidationResponse = service.userExistence(user.getEmail());
            if (emailValidationResponse) {
                return new ResponseEntity<>("Email already in use", HttpStatus.CONFLICT);
            }
            // Add the user
            service.register(user);
            mailService.sendSimpleEmail(user.getEmail(), "Welcome to Appoint Doctor",
                    "Thank you for registering!");
            return new ResponseEntity<>("Account Created", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String email = loginRequest.getEmail();
                String accessToken = jwtUtil.generateAccessToken(email);
                String refreshToken = jwtUtil.generateRefreshToken(email);
                Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                refreshCookie.setAttribute("SameSite", "None");
                response.addCookie(refreshCookie);

                Users user = userRepo.findByEmail(email);
                LoginResponse loginResponse = null;
                if (user.getRoles().contains(Role.DOCTOR)) {
                    Doctor doctor = doctorRepo.findByEmail(loginRequest.getEmail());
                    loginResponse = new LoginResponse(
                            "Login successful",
                            HttpStatus.OK.value(),
                            accessToken,
                            refreshToken,
                            loginRequest.getEmail(),
                            user.getRoles(),
                            user.getFirstName() + " " + user.getLastName(),
                            doctor.getDoctorId(),
                            doctor.getAccountStatus()
                    );
                    return ResponseEntity.ok(loginResponse);
                }

                loginResponse = new LoginResponse(
                        "Login successful",
                        HttpStatus.OK.value(),
                        accessToken,
                        refreshToken,
                        loginRequest.getEmail(),
                        user.getRoles(),
                        user.getFirstName() + " " + user.getLastName()
                );
                return ResponseEntity.ok(loginResponse);
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }


    @PostMapping("/forget")
    public ResponseEntity<?> forgetPassword(@RequestBody Users user){
        boolean emailValidationResponse = service.userExistence(user.getEmail());
        if (emailValidationResponse) {
            int otp = generateSixDigitOtp();
            mailService.sendSimpleEmail(user.getEmail(), "Appoint Doctor - Recover Your Account",
                    "Otp for recovering account: "+ otp);
            service.addOtp(user,otp);
            return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // Delete cookie
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);

        // Invalidate refresh token from server side
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                    jwtUtil.invalidateRefreshToken(cookie.getValue());
                    break;
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // Extract refresh token from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token not found"));
        }

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }

            String email = jwtUtil.extractEmail(refreshToken);

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(email);

            // Optionally generate new refresh token (token rotation)
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            // Invalidate old refresh token
            jwtUtil.invalidateRefreshToken(refreshToken);

            // Set new refresh token cookie
            Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
            newRefreshCookie.setHttpOnly(true);
            newRefreshCookie.setSecure(true); // Set to true in production
            newRefreshCookie.setPath("/");
            newRefreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            newRefreshCookie.setAttribute("SameSite", "None");
            response.addCookie(newRefreshCookie);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "message", "Token refreshed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed"));
        }
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