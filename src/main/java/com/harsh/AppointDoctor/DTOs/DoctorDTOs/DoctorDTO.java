package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import lombok.Data;

import java.util.List;

@Data
public class DoctorDTO {
    private DoctorPersonalProfileResponse personalInfo;
    private DoctorProfessionalDetailsResponse professionalInfo;
    private List<DoctorEducationalDetailsResponse> education;
    private DoctorClinicResponse clinicInfos;
    private List<DoctorDocumentResponse> documents;
}

