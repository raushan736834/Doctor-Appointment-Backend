package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.*;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorLoginResponse;
import com.harsh.AppointDoctor.Enums.AccountStatus;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthController {

    private final AuthService service;
    private final JWTService jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;
    private final AuthenticationManager authManager;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(HttpServletRequest request) {
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
                    .body(ApiResponse.error("Refresh token not found",401));
        }

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error( "Invalid or expired refresh token",401));
            }

            String email = jwtUtil.extractEmail(refreshToken);

            ApiResponse<?> userInfo = service.getCurrentUser(email);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed",401));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody Users user) {
        try {
            boolean emailValidationResponse = service.userExistence(user.getEmail());
            if (emailValidationResponse) {
                return new ResponseEntity<>(ApiResponse.error("Email already in use",409), HttpStatus.CONFLICT);
            }
            // Add the user
            service.register(user);
            mailService.sendSimpleEmail(user.getEmail(), "Welcome to Appoint Doctor",
                    "Thank you for registering!");
            return new ResponseEntity<>(ApiResponse.success(null,"Account Created",200), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(ApiResponse.error("Error occurred during creating account",500),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
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
                DoctorLoginResponse doctorLoginResponse = null;
                if(user.getRoles().contains(Role.ADMIN)){
                    LoginResponse adminLoginResponse = new LoginResponse(
                            "Login successful",
                            accessToken,
                            refreshToken,
                            loginRequest.getEmail(),
                            user.getRoles(),
                            user.getFirstName() + " " + user.getLastName()
                    );
                    return ResponseEntity.ok(ApiResponse.success(adminLoginResponse,"Login Successful",200));
                }
                if (user.getRoles().contains(Role.DOCTOR)) {
                    Doctor doctor = doctorRepo.findByEmail(loginRequest.getEmail());
                    if(doctor.getAccountStatus() == AccountStatus.COMPLETE){
                        doctorLoginResponse = new DoctorLoginResponse(
                                user.getFirstName() + " " + user.getLastName(),
                                loginRequest.getEmail(),
                                doctor.getDoctorId(),
                                accessToken,
                                user.getRoles(),
                                doctor.getAccountStatus(),
                                "Login successful",
                                refreshToken,
                                doctor.getProfessional().getConsultationFees()
                        );
                    } else {
                        doctorLoginResponse = new DoctorLoginResponse(
                                user.getFirstName() + " " + user.getLastName(),
                                loginRequest.getEmail(),
                                doctor.getDoctorId(),
                                accessToken,
                                user.getRoles(),
                                doctor.getAccountStatus(),
                                "Login successful",
                                refreshToken,
                                0
                        );
                    }
                    return ResponseEntity.ok(ApiResponse.success(doctorLoginResponse,"Login Successful",20));
                }
                LoginResponse userLoginResponse = null;
                userLoginResponse = new LoginResponse(
                        "Login successful",
                        accessToken,
                        refreshToken,
                        loginRequest.getEmail(),
                        user.getRoles(),
                        user.getFirstName() + " " + user.getLastName()
                );
                return ResponseEntity.ok(ApiResponse.success(userLoginResponse,"Login Successful",200));
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid credentials",401));
            }
        } catch (AuthenticationException e) {
            log.error(e.getMessage(),e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials",401));
        }
    }


    @PostMapping("/forget")
    public ResponseEntity<ApiResponse<?>> forgetPassword(@RequestBody Users user){
        boolean emailValidationResponse = service.userExistence(user.getEmail());
        if (emailValidationResponse) {
            int otp = generateSixDigitOtp();
            mailService.sendSimpleEmail(user.getEmail(), "Appoint Doctor - Recover Your Account",
                    "Otp for recovering account: "+ otp);
            service.addOtp(user,otp);
            return new ResponseEntity<>(ApiResponse.success(null,"Account Recovered",200),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(ApiResponse.error("Email not found",404), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/forget/verify")
    public ResponseEntity<ApiResponse<?>> validateOTP(@RequestBody Users user){
        return service.validateOtp(user);
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody Users user){
        return service.updatePassword(user);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear refresh token cookie
        try {
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

            return ResponseEntity.ok(ApiResponse.success( null,"Logged out successfully",200));
        } catch (Exception e) {
            throw new RuntimeException("Error during logout");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest request, HttpServletResponse response) {
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
                    .body(ApiResponse.error( "Refresh token not found",401));
        }

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error( "Invalid or expired refresh token",401));
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

            return ResponseEntity.ok(ApiResponse.success(newAccessToken, "Token refreshed successfully",200));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error( "Token refresh failed",401));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid or missing token",401));
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        try {
            service.changePassword(email, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null,"Password changed successfully",200));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(),400));
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}