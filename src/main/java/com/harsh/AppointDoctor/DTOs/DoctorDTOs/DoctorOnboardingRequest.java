package com.harsh.AppointDoctor.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class DoctorOnboardingRequest {
    private String doctorId;
    private String profileImage;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String pincode;

    private ProfessionalDTO professional;

    private List<EducationDTO> doctorEducation;

    private ClinicInfoDTO clinicInfos;

    // ---------- Inner DTOs ----------

    @Data
    public static class ProfessionalDTO {
        private String medicalLicenseNumber;
        private String yearOfExp;
        private String specialization;
        private int consultationFees;
        private String currentHospital;
        private String medicalCouncil;
        private String languageKnown;
    }

    @Data
    public static class EducationDTO {
        private String degreeName;
        private String schoolName;
        private int completionYear;
    }

    @Data
    public static class ClinicInfoDTO {
        private String clinicName;
        private String clinicType;
        private String clinicPhone;
        private String clinicCity;
        private String clinicState;
        private String clinicAddress;
        private String clinicPincode;
        private String consultationDuration;

        private List<OperationHoursDTO> operatingHours;
    }

    @Data
    public static class OperationHoursDTO {
        private String days;
        private String open;
        private String close;
        private Boolean isClosedToday;
    }
}

