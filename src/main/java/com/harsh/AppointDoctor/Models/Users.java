package com.harsh.AppointDoctor.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String roles;
    private int otp;
}
