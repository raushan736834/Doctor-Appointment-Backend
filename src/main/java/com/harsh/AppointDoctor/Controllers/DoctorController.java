package com.harsh.AppointDoctor.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.*;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorOnboardingRequest;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorClinicInfo;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorEducation;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorProfessional;
import com.harsh.AppointDoctor.Services.DoctorOnboardingService.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PutMapping("/personalDetails")
    public ResponseEntity<ApiResponse<?>> addPersonalDetails(
            @RequestPart("doctor") String doctorJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Principal principal) {
        try {
            String email = principal.getName();
            Doctor doctor = new ObjectMapper().readValue(doctorJson, Doctor.class);

            if (profileImage != null && !profileImage.isEmpty()) {
//                doctor.setProfileImage(profileImage.getBytes());
            }

            doctorService.saveBasicDetails(doctor, email);
            return new ResponseEntity<>(ApiResponse.success(null,"Personal Details Successfully Added",201), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),401), HttpStatus.UNAUTHORIZED);
        }
    }


    @PutMapping("/professional")
    public ResponseEntity<ApiResponse<?>> addProfessionalDetails(@RequestBody DoctorProfessional professional,Principal principal) {
        try {
            String email = principal.getName();
            doctorService.addProfessionalDetails(professional,email);
            return new ResponseEntity<>(ApiResponse.success(null, "Professional Details Added",201),HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),401), HttpStatus.UNAUTHORIZED);
        }
    }

    // Step 3: Add availability slots
    @PutMapping("/education")
    public ResponseEntity<ApiResponse<?>> addEducationalDetails(@RequestBody List<DoctorEducation> educations,Principal principal) {
        try {
            String email = principal.getName();
            doctorService.addEducationalDetails(educations,email);
            return new ResponseEntity<>(ApiResponse.success(null,"Educational Details Added",201),HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),401), HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/clinicInfo")
    public ResponseEntity<?> addClinicInfo(@RequestBody DoctorClinicInfo clinicInfo, Principal principal) {
        System.out.println(clinicInfo);
        try {
            String email = principal.getName();
            ApiResponse<?> response = doctorService.addClinicInfos(clinicInfo,email);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 4: Add documents
    @PutMapping("/documents")
    public ResponseEntity<ApiResponse<?>> uploadDocuments(
            @RequestParam(value = "medicalLicense") MultipartFile medicalLicense,
            @RequestParam(value = "boardCertificate") MultipartFile boardCertificate,
            @RequestParam(value = "malpracticeInsurance") MultipartFile malpracticeInsurance,
            @RequestParam(value = "cv", required = false) MultipartFile cv,
            Principal principal) {

        // Collect all files in one place and filter out null/empty ones
        List<MultipartFile> documents = Stream.of(
                        medicalLicense,
                        boardCertificate,
                        malpracticeInsurance,
                        cv
                )
                .filter(Objects::nonNull)
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toList());

        String email = principal.getName();

        ApiResponse<?> response = doctorService.addDocuments(documents, email);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }



    @GetMapping("/review")
    public ResponseEntity<ApiResponse<?>> reviewStage(Principal principal){
        try{
            String email = principal.getName();
            doctorService.reviewAccount(email);
            return new ResponseEntity<>(ApiResponse.success(null,"Successfully OnBoarding",200), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),500), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/saveAllDoctors")
    public ResponseEntity<ApiResponse<?>> saveAllDoctors(@RequestBody List<Doctor> doctors){
        try {
            doctorService.saveAllDoctors(doctors);
            return ResponseEntity.ok(ApiResponse.success(null,"All Doctors Saved Successfully",200));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Something went wrong: " + e.getMessage(), 500));
        }
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<?>> saveOnboarding(@RequestBody DoctorOnboardingRequest request) {
        Doctor saved = doctorService.saveDoctorOnboarding(request);
        return ResponseEntity.ok(ApiResponse.success(saved,"",200));
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
