package com.harsh.AppointDoctor.DTOs.DoctorDTOs;

import lombok.Data;

@Data
public class DoctorPersonalProfileResponse {
    private String doctorId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String dob;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
}
