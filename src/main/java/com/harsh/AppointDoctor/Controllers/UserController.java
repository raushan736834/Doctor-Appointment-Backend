package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Repo.UserProfileRepo;
import com.harsh.AppointDoctor.Utility.ErrorResponse;
import com.harsh.AppointDoctor.Utility.LoginResponse;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.UserRepo;
import com.harsh.AppointDoctor.Services.MailService;
import com.harsh.AppointDoctor.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.harsh.AppointDoctor.Utility.OtpGenerator.generateSixDigitOtp;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private MailService mailService;

    @PostMapping("/auth.signup.user")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            // Check if the email already exists
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

//    @PostMapping("/auth.login.user")
//    public ResponseEntity<?> authoriseUser(@RequestBody Users loginRequest){
//        boolean emailAuthorisationResponse = service.authenticateUser(loginRequest);
//        if (emailAuthorisationResponse){
//            return new ResponseEntity<>("Login Successful",HttpStatus.OK);
//        }
//        else {
//            return new ResponseEntity<>("Invalid Username or Password",HttpStatus.NOT_FOUND);
//        }
//    }

    @PostMapping("/auth.login.user")
    public ResponseEntity<?> authoriseUser(@RequestBody Users loginRequest) {
        try {
            String token = service.verify(loginRequest);  // The `verify` method will either return a token or throw an exception
            if (!token.equals("fail")) {
                return new ResponseEntity<>(new LoginResponse("Login Successful", HttpStatus.OK.value(),
                        token,loginRequest.getEmail()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Invalid Username or Password", HttpStatus.UNAUTHORIZED.value()),
                        HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostMapping("/auth.login.user")
//    public ResponseEntity<?> authoriseUser(@RequestBody Users loginRequest) {
//        try {
//            String token = service.verify(loginRequest);  // The `verify` method will either return a token or throw an exception
//            ResponseCookie jwtCookie = ResponseCookie.from("token", token)
//                    .httpOnly(true)
//                    .secure(false)
//                    .path("/")
//                    .maxAge(24 * 60 * 60)
//                    .sameSite("Strict")
//                    .build();
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
//                    .body("Login Successful");
//        } catch (Exception e) {
//            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

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
