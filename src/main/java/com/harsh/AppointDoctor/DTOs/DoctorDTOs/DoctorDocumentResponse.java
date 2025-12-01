package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import jakarta.persistence.Lob;
import lombok.Data;

@Data
public class DoctorDocumentResponse {
    private String fileType;
    private String fileName;

    @Lob
    private byte[] fileData;
}
