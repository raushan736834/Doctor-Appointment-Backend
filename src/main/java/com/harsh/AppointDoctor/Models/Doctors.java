package com.harsh.AppointDoctor.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Doctors {

    @Id
    private String email;

    private String fullName;
    private String password;
    private int otp;
}
