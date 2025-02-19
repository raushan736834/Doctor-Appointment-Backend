package com.harsh.AppointDoctor.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UsersProfile {

    @Id
    private String email;

    private String fullName;
    private String gender;
//    @Column(name = "Date Of Birth")
//    private String dob;
    private String phone;

    private String address;
    private String city;
    private String country;
    private String state;
    private Long pincode;


}
