package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import com.harsh.AppointDoctor.Enums.AccountStatus;
import lombok.Data;

import java.util.List;

@Data
public class DoctorDTO {
    private DoctorPersonalProfileResponse personalInfo;
    private DoctorProfessionalDetailsResponse professionalInfo;
    private List<DoctorEducationalDetailsResponse> education;
    private DoctorClinicResponse clinicInfos;
//    private List<DoctorDocument> documents;
}

