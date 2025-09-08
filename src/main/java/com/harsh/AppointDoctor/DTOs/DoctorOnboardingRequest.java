package com.harsh.AppointDoctor.DTOs;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class DoctorOnboardingRequest {
    private String email;
    private String doctorName;
    private String phoneNumber;
    private String specialization;
    private int consultationFees;
    private int experienceYears;
    private String profilePhoto;
    private String gender;
    private String locality;
    private String clinicName;
    private String state;
    private String pincode;

        // City info (instead of whole City object, frontend will send raw values
    private String cityName;
    private String country;
    // If qualifications are added at registration time
    private String[] qualifications;
}
