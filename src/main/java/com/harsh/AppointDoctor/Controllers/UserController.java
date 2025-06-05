package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Utility.ErrorResponse;
import com.harsh.AppointDoctor.Utility.LoginResponse;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import static com.harsh.AppointDoctor.Utility.OtpGenerator.generateSixDigitOtp;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService service;


    @Autowired
    private MailService mailService;

    @PostMapping("/auth/signup")
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
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostMapping("/auth/login")
//    public ResponseEntity<?> authoriseUser(@RequestBody Users loginRequest) {
//        try {
//            LoginResponse loginResponse = service.verify(loginRequest);// Now returns an object
//            System.out.println(loginResponse);
//            if (loginResponse != null) {
//                return new ResponseEntity<>(loginResponse, HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(new ErrorResponse("Invalid Username or Password", HttpStatus.UNAUTHORIZED.value()),
//                        HttpStatus.UNAUTHORIZED);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity<>(new ErrorResponse("Login Failed", HttpStatus.INTERNAL_SERVER_ERROR.value()),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
@PostMapping("/auth/login")
public ResponseEntity<?> authoriseUser(@RequestBody Users loginRequest) {
    try {
        // Check if email or password is missing (simple client-side validation fallback)
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Missing email or password", HttpStatus.BAD_REQUEST.value()));
        }
        ResponseEntity<?> loginResponse = service.verify(loginRequest);
        if (loginResponse != null && loginResponse.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
        }
    } catch (AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Authentication failed", HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Login failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
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

    @GetMapping("/hello")
    public String hello(){
        return "Hello";
    }
    @PostMapping("/forget/verify")
    public ResponseEntity<?> validateOTP(@RequestBody Users user){
        return service.validateOtp(user);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Users user){
        return service.updatePassword(user);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<String> getUser(@PathVariable String email){
        return service.getUserById(email);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Only for HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0);  // Clear the cookie

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}
