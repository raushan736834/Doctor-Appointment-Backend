package com.harsh.AppointDoctor.Services.DoctorOnboardingService;


import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.*;
import com.harsh.AppointDoctor.DTOs.DoctorOnboardingRequest;
import com.harsh.AppointDoctor.Enums.AccountStatus;
import com.harsh.AppointDoctor.Enums.Days;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.*;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            List<DoctorDocument> existingDocs = existingDoctor.getDocuments();

            for (MultipartFile file : files) {
                String newFileName = file.getOriginalFilename();

                DoctorDocument existing = existingDocs.stream()
                        .filter(doc -> doc.getFileName().equals(newFileName))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    // Update existing document data
                    existing.setFileType(file.getContentType());
                    existing.setFileData(file.getBytes());
                } else {
                    // Add as new document
                    DoctorDocument newDoc = new DoctorDocument();
                    newDoc.setDoctor(existingDoctor);
                    newDoc.setFileName(newFileName);
                    newDoc.setFileType(file.getContentType());
                    newDoc.setFileData(file.getBytes());
                    existingDocs.add(newDoc);
                }
            }
            doctorRepo.save(existingDoctor);
            existingDoctor.setAccountStatus(AccountStatus.DOCUMENT);
            return ApiResponse.success(null, "Documents uploaded successfully.", 201);
        } catch (RuntimeException | IOException e) {
            return ApiResponse.error("Failed to upload documents:", 500);
        }
    }

    @Transactional
    public ApiResponse<?> addClinicInfos(DoctorClinicInfo clinicInfos, String email) {
        try {
            Doctor doctor = doctorRepo.findByEmail(email);
            if (doctor == null) {
                throw new RuntimeException("Doctor not found");
            }

            // Check if clinic info already exists
            DoctorClinicInfo existingClinicInfo = doctor.getClinicInfos();

            if (existingClinicInfo != null) {
                // UPDATE existing clinic info
                existingClinicInfo.setClinicName(clinicInfos.getClinicName());
                existingClinicInfo.setClinicType(clinicInfos.getClinicType());
                existingClinicInfo.setClinicPhone(clinicInfos.getClinicPhone());
                existingClinicInfo.setClinicEmail(clinicInfos.getClinicEmail());
                existingClinicInfo.setEstablishedYear(clinicInfos.getEstablishedYear());
                existingClinicInfo.setClinicAddress(clinicInfos.getClinicAddress());
                existingClinicInfo.setClinicCity(clinicInfos.getClinicCity());
                existingClinicInfo.setClinicState(clinicInfos.getClinicState());
                existingClinicInfo.setClinicPincode(clinicInfos.getClinicPincode());
                existingClinicInfo.setConsultationDuration(clinicInfos.getConsultationDuration());

                // Update operating hours
                if (clinicInfos.getOperatingHours() != null) {
                    existingClinicInfo.getOperatingHours().clear();
                    existingClinicInfo.getOperatingHours().addAll(clinicInfos.getOperatingHours());
                }
                System.out.println(existingClinicInfo);
            } else {
                // CREATE new clinic info
                clinicInfos.setDoctor(doctor);
                doctor.setClinicInfos(clinicInfos);
            }
            System.out.println(doctor);
            doctor.setAccountStatus(AccountStatus.CLINIC);
            doctorRepo.save(doctor);
            return ApiResponse.success(null,"Clinic Details Added Successfully", 201);
        } catch (RuntimeException e) {
            return ApiResponse.error("Failed to add Clinic Information", 500);

        }
    }

    public void reviewAccount(String email) {
        Doctor existingDoctor  = doctorRepo.findByEmail(email);
        if (existingDoctor == null){
            throw new RuntimeException("Doctor not found");
        }
        System.out.println("here");
        existingDoctor.setAccountStatus(AccountStatus.PENDING);
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

//        Map clinic info
        // Map clinic info
        DoctorClinicInfo clinicInfos = existingDoctor.getClinicInfos();
        if (clinicInfos != null) {
            DoctorClinicResponse clinicResponse = new DoctorClinicResponse();

            // Manually map simple fields
            clinicResponse.setClinicName(clinicInfos.getClinicName());
            clinicResponse.setClinicType(clinicInfos.getClinicType());
            clinicResponse.setClinicPhone(clinicInfos.getClinicPhone());
            clinicResponse.setClinicEmail(clinicInfos.getClinicEmail());
            clinicResponse.setEstablishedYear(clinicInfos.getEstablishedYear());
            clinicResponse.setClinicAddress(clinicInfos.getClinicAddress());
            clinicResponse.setClinicCity(clinicInfos.getClinicCity());
            clinicResponse.setClinicState(clinicInfos.getClinicState());
            clinicResponse.setClinicPincode(clinicInfos.getClinicPincode());
            clinicResponse.setConsultationDuration(clinicInfos.getConsultationDuration());

            // Manually map operating hours to avoid ModelMapper issues
            if (clinicInfos.getOperatingHours() != null) {
                List<OperatingHoursResponse> operatingHoursList = clinicInfos.getOperatingHours().stream()
                        .map(op -> {
                            OperatingHoursResponse response = new OperatingHoursResponse();
                            response.setDays(op.getDays());
                            response.setOpen(op.getOpen());
                            response.setClose(op.getClose());
                            response.setIsClosedToday(op.getIsClosedToday());
                            return response;
                        })
                        .toList();
                clinicResponse.setOperatingHours(operatingHoursList);
            }
            doctorDTO.setClinicInfos(clinicResponse);
        }

        List<DoctorDocument> doctorDocuments = existingDoctor.getDocuments();
        if(doctorDocuments != null && !doctorDocuments.isEmpty()){
            List<DoctorDocumentResponse> doctorDocumentResponses = doctorDocuments.stream()
                    .map(doctorDocument -> modelMapper.map(doctorDocument, DoctorDocumentResponse.class))
                    .toList();
            doctorDTO.setDocuments(doctorDocumentResponses);
        }

        return ResponseEntity.ok(ApiResponse.success(doctorDTO, "Doctor details fetched successfully", 200));
    }

    public void saveAllDoctors(List<Doctor> doctors) {
        doctorRepo.saveAll(doctors);
    }

    public void saveDoctorList(List<DoctorOnboardingRequest> requests) {
        List<Doctor> doctors = new ArrayList<>();
        for (DoctorOnboardingRequest req : requests) {
            doctors.add(saveDoctorOnboarding(req));
        }
        doctorRepo.saveAll(doctors);
    }

    public Doctor saveDoctorOnboarding(DoctorOnboardingRequest request) {
        Doctor doctor = new Doctor();
        String generatedId = request.getDoctorId() == null || request.getDoctorId().isEmpty()
                ? UUID.randomUUID().toString()
                : request.getDoctorId();
        doctor.setDoctorId(generatedId);
//        doctor.setProfileImage(request.getProfileImage());
        doctor.setEmail(request.getEmail());
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPhone(request.getPhone());
        doctor.setDob(request.getDob());
        doctor.setGender(request.getGender());
        doctor.setAddress(request.getAddress());
        doctor.setCity(request.getCity());
        doctor.setState(request.getState());
        doctor.setPincode(request.getPincode());
        doctor.setAccountStatus(AccountStatus.COMPLETE); // optional

        // ---------- PROFESSIONAL ----------
        DoctorProfessional professional = new DoctorProfessional();
        professional.setMedicalLicenseNumber(request.getProfessional().getMedicalLicenseNumber());
        professional.setYearOfExp(String.valueOf(request.getProfessional().getYearOfExp()));
        professional.setSpecialization(request.getProfessional().getSpecialization());
        professional.setConsultationFees(request.getProfessional().getConsultationFees());
        professional.setCurrentHospital(request.getProfessional().getCurrentHospital());
        professional.setMedicalCouncil(request.getProfessional().getMedicalCouncil());
        professional.setLanguageKnown(request.getProfessional().getLanguageKnown());
        professional.setDoctor(doctor);
        doctor.setProfessional(professional);

        // ---------- EDUCATION ----------
        List<DoctorEducation> educationList = new ArrayList<>();
        for (var edu : request.getDoctorEducation()) {

            DoctorEducation e = new DoctorEducation();
            e.setDegreeName(edu.getDegreeName());
            e.setSchoolName(edu.getSchoolName());
            e.setCompletionYear(edu.getCompletionYear());
            e.setDoctor(doctor);

            educationList.add(e);
        }
        doctor.setDoctorEducation(educationList);

        // ---------- CLINIC INFO ----------
        try {
            DoctorClinicInfo clinic = new DoctorClinicInfo();
            clinic.setClinicName(request.getClinicInfos().getClinicName());
            clinic.setClinicType(request.getClinicInfos().getClinicType());
            clinic.setClinicPhone(request.getClinicInfos().getClinicPhone());
            clinic.setClinicCity(request.getClinicInfos().getClinicCity());
            clinic.setClinicState(request.getClinicInfos().getClinicState());
            clinic.setClinicAddress(request.getClinicInfos().getClinicAddress());
            clinic.setClinicPincode(request.getClinicInfos().getClinicPincode());
            clinic.setConsultationDuration(request.getClinicInfos().getConsultationDuration());
            clinic.setDoctor(doctor);

            // ---------- OPERATION HOURS ----------
            List<OperationHours> hoursList = new ArrayList<>();

            for (var oh : request.getClinicInfos().getOperatingHours()) {
                OperationHours ohEntity = new OperationHours();
                ohEntity.setDays(Days.valueOf(oh.getDays()));
                ohEntity.setOpen(oh.getOpen());
                ohEntity.setClose(oh.getClose());
                ohEntity.setIsClosedToday(oh.getIsClosedToday());
                hoursList.add(ohEntity);
            }
            clinic.setOperatingHours(hoursList);

            doctor.setClinicInfos(clinic);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        // ---------- SAVE EVERYTHING ----------
        return doctorRepo.save(doctor);
    }
}
