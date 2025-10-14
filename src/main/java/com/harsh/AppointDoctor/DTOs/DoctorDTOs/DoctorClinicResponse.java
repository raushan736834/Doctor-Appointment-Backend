package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import com.harsh.AppointDoctor.Models.DoctorOnboarding.OperationHours;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DoctorClinicResponse {
    private String clinicName;
    private String clinicType;
    private String clinicPhone;
    private String clinicEmail;
    private String establishedYear;
    private String clinicAddress;
    private String clinicCity;
    private String clinicState;
    private String clinicPincode;
    private String consultationDuration;
    private List<OperationHours> operatingHours = new ArrayList<>();
}
