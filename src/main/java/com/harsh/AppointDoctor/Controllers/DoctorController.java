package com.harsh.AppointDoctor.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.*;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.*;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Services.DoctorOnboardingService.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PutMapping("/personalDetails")
    public ResponseEntity<?> addPersonalDetails(
            @RequestPart("doctor") String doctorJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Principal principal) {
        try {
            String email = principal.getName();
            Doctor doctor = new ObjectMapper().readValue(doctorJson, Doctor.class);

            if (profileImage != null && !profileImage.isEmpty()) {
                doctor.setProfileImage(profileImage.getBytes());
            }

            doctorService.saveBasicDetails(doctor, email);
            return new ResponseEntity<>("Personal Details Successfully Added", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/professional")
    public ResponseEntity<?> addProfessionalDetails(@RequestBody DoctorProfessional professional,Principal principal) {
        try {
            String email = principal.getName();
            doctorService.addProfessionalDetails(professional,email);
            return new ResponseEntity<>("Professional Details Added",HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 3: Add availability slots
    @PutMapping("/education")
    public ResponseEntity<?> addEducationalDetails(@RequestBody List<DoctorEducation> educations,Principal principal) {
        try {
            String email = principal.getName();
            doctorService.addEducationalDetails(educations,email);
            return new ResponseEntity<>("Educational Details Added",HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/clinicInfo")
    public ResponseEntity<?> addClinicInfo(@RequestBody DoctorClinicInfo clinicInfo, Principal principal) {
        try {
            String email = principal.getName();
            doctorService.addClinicInfos(clinicInfo,email);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 4: Add documents
    @PutMapping("/documents")
    public ResponseEntity<ApiResponse<?>> addDocuments(
            @RequestParam("files") List<MultipartFile> files,
            Principal principal) {

            String email = principal.getName();
            ApiResponse<?> response = doctorService.addDocuments(files, email);
            // This makes the response consistent for both success and error cases
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {

                return ResponseEntity.badRequest().body(response);
            }
    }


    @GetMapping("/review")
    public ResponseEntity<?> reviewStage(Principal principal){
        try{
            String email = principal.getName();
            doctorService.reviewAccount(email);
            return new ResponseEntity<>("Successfully OnBoarding", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getDoctorDetails")
    public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorDetails(Principal principal){
        try {
            String email = principal.getName();
            return doctorService.getDoctorDetails(email);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(),500));
        }
    }

    @GetMapping("/getPersonalDetails")
    public ResponseEntity<ApiResponse<DoctorPersonalProfileResponse>> getPersonalDetails(Principal principal) {
        try {
            String email = principal.getName();
            return doctorService.getPersonalDetails(email);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Something went wrong: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/getProfessionalDetails")
    public ResponseEntity<ApiResponse<DoctorProfessionalDetailsResponse>> getProfessionalDetails(Principal principal){
        try {
            String email = principal.getName();
            return doctorService.getProfessionalDetails(email);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(),500));
        }
    }

    @GetMapping("/getEducationalDetails")
    public ResponseEntity<ApiResponse<List<DoctorEducationalDetailsResponse>>> getEducationalDetails(Principal principal){
        try {
            String email = principal.getName();
            return doctorService.getEducationalDetails(email);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(),500));
        }
    }

    @GetMapping("/getClinicDetails")
    public ResponseEntity<ApiResponse<DoctorClinicResponse>> getClinicDetails(Principal principal){
        try {
            String email = principal.getName();
            return doctorService.getClinicDetails(email);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(),500));
        }
    }
}
