package com.harsh.AppointDoctor.Services.DoctorOnboardingService;


import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.*;
import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.*;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepo doctorRepo;
    private final ModelMapper modelMapper;

    public void saveBasicDetails(Doctor doctor, String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor != null) {
            existingDoctor.setAccountStatus(AccountStatus.PERSONAL);
            existingDoctor.setPhone(doctor.getPhone());
            existingDoctor.setDob(doctor.getDob());
            existingDoctor.setGender(doctor.getGender());
            existingDoctor.setAddress(doctor.getAddress());
            existingDoctor.setCity(doctor.getCity());
            existingDoctor.setState(doctor.getState());
            existingDoctor.setPincode(doctor.getPincode());
            if (doctor.getProfileImage() != null && doctor.getProfileImage().length > 0) {
                existingDoctor.setProfileImage(doctor.getProfileImage());
            }
            doctorRepo.save(existingDoctor);
        }else {
            throw new RuntimeException("Doctor not found");
        }
    }

    public void addProfessionalDetails(DoctorProfessional professional, String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        // if already exists, update it instead of insert
        DoctorProfessional existingProf = existingDoctor.getProfessional();
        if (existingProf != null) {
            professional.setId(existingProf.getId()); // ensure update
        }
        existingDoctor.setAccountStatus(AccountStatus.PROFESSIONAL);
        professional.setDoctor(existingDoctor);
        existingDoctor.setProfessional(professional);
        doctorRepo.save(existingDoctor);

    }

    public void addEducationalDetails(List<DoctorEducation> educations, String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null) {
            throw new RuntimeException("Doctor not found");
        }
        existingDoctor.setAccountStatus(AccountStatus.EDUCATION);
        educations.forEach(edu -> edu.setDoctor(existingDoctor));
        existingDoctor.getDoctorEducation().addAll(educations);

        doctorRepo.save(existingDoctor);
    }

    public ApiResponse<?> addDocuments(List<MultipartFile> files, String email) {
        // Use the custom query to fetch the doctor with documents
        try {
            Doctor existingDoctor = doctorRepo.findByEmailWithDocuments(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            existingDoctor.setAccountStatus(AccountStatus.DOCUMENT);

            List<DoctorDocument> docs = new ArrayList<>();
            for (MultipartFile file : files) {
                DoctorDocument doc = new DoctorDocument();
                doc.setDoctor(existingDoctor);
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setFileData(file.getBytes());
                docs.add(doc);
            }
            existingDoctor.getDocuments().addAll(docs);
            doctorRepo.save(existingDoctor);
            return ApiResponse.success(null, "Documents uploaded successfully.", 201);
        } catch (RuntimeException | IOException e) {
            return ApiResponse.error("Failed to upload documents:", 500);
        }
    }

    public void addClinicInfos(DoctorClinicInfo clinicInfos, String email) {
        Doctor doctor = doctorRepo.findByEmail(email);
        if (doctor == null) throw new RuntimeException("Doctor not found");
        clinicInfos.setDoctor(doctor);
        doctor.setAccountStatus(AccountStatus.CLINIC);
        doctor.setClinicInfos(clinicInfos);
        doctorRepo.save(doctor);
    }

    public void reviewAccount(String email) {
        Doctor existingDoctor  = doctorRepo.findByEmail(email);
        if (existingDoctor == null){
            throw new RuntimeException("Doctor not found");
        }
        existingDoctor.setAccountStatus(AccountStatus.COMPLETE);
        doctorRepo.save(existingDoctor);
    }

    public ResponseEntity<ApiResponse<DoctorPersonalProfileResponse>> getPersonalDetails(String email){
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No Doctor Details Found", 404));
        }
        DoctorPersonalProfileResponse profileResponse =
                modelMapper.map(existingDoctor, DoctorPersonalProfileResponse.class);

        return ResponseEntity
                .ok(ApiResponse.success(profileResponse, "Doctor data fetched successfully", 200));
    }

    public ResponseEntity<ApiResponse<DoctorProfessionalDetailsResponse>> getProfessionalDetails(String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No Doctor Details Found",404));
        }
        DoctorProfessional details = existingDoctor.getProfessional();
        if (details == null){
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null, "No Professional detail found", 204));
        }

        DoctorProfessionalDetailsResponse professionalResponse =
                modelMapper.map(details, DoctorProfessionalDetailsResponse.class);

        return ResponseEntity.ok(ApiResponse.success(professionalResponse, "Doctor Professional Data fetched successfully", 200));
    }


    public ResponseEntity<ApiResponse<List<DoctorEducationalDetailsResponse>>> getEducationalDetails(String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);

        if (existingDoctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No Doctor Details Found", 404));
        }

        List<DoctorEducation> educationList = existingDoctor.getDoctorEducation();

        if (educationList == null || educationList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null, "No educational details found", 204));
        }

        List<DoctorEducationalDetailsResponse> educationalResponses = educationList.stream()
                .map(education -> modelMapper.map(education, DoctorEducationalDetailsResponse.class))
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(educationalResponses, "Doctor educational details fetched successfully", 200)
        );
    }

    public ResponseEntity<ApiResponse<DoctorClinicResponse>> getClinicDetails(String email){
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null){
            return ResponseEntity.status(404).body(ApiResponse.error("No Doctor Data Found", 404));
        }

        DoctorClinicInfo clinicDetails = existingDoctor.getClinicInfos();
        if (clinicDetails == null){
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null,"No Clinic Details Found", 204));
        }
        DoctorClinicResponse clinicResponse = modelMapper.map(clinicDetails, DoctorClinicResponse.class);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(clinicResponse,"Doctor Clinic Details fetched successfully", 200));

    }

    public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorDetails(String email) {
        Doctor existingDoctor = doctorRepo.findByEmail(email);
        if (existingDoctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No Doctor Details Found", 404));
        }

        DoctorDTO doctorDTO = new DoctorDTO();

        // Map personal info
        DoctorPersonalProfileResponse personalInfo = modelMapper.map(existingDoctor, DoctorPersonalProfileResponse.class);
        doctorDTO.setPersonalInfo(personalInfo);

        // Map professional info
        DoctorProfessional professional = existingDoctor.getProfessional();
        if (professional != null) {
            DoctorProfessionalDetailsResponse professionalInfo = modelMapper.map(professional, DoctorProfessionalDetailsResponse.class);
            doctorDTO.setProfessionalInfo(professionalInfo);
        }

        // Map educational details
        List<DoctorEducation> educationList = existingDoctor.getDoctorEducation();
        if (educationList != null && !educationList.isEmpty()) {
            List<DoctorEducationalDetailsResponse> educationalResponses = educationList.stream()
                    .map(education -> modelMapper.map(education, DoctorEducationalDetailsResponse.class))
                    .toList();
            doctorDTO.setEducation(educationalResponses);
        }

        // Map clinic info
        DoctorClinicInfo clinicInfos = existingDoctor.getClinicInfos();
        if (clinicInfos != null) {
            DoctorClinicResponse clinicResponse = modelMapper.map(clinicInfos, DoctorClinicResponse.class);
            doctorDTO.setClinicInfos(clinicResponse);
        }

        return ResponseEntity.ok(ApiResponse.success(doctorDTO, "Doctor details fetched successfully", 200));
    }
}
