package com.harsh.AppointDoctor.Models.DoctorOnboarding;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
public class DoctorDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileType;
    private String fileName;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Doctor doctor;
}
