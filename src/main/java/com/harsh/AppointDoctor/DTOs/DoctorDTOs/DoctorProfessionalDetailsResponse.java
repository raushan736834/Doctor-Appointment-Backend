package com.harsh.AppointDoctor.DTOs.DoctorDTOs;


import lombok.Data;

@Data
public class DoctorProfessionalDetailsResponse {
    private String medicalLicenseNumber;
    private String yearOfExp;
    private String specialization;
    private String subSpeciality;
    private int consultationFees;
    private String currentHospital;
    private String medicalCouncil;
    private String languageKnown;
}
