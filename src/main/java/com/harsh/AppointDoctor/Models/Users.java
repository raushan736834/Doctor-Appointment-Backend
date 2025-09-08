package com.harsh.AppointDoctor.Models;

import com.harsh.AppointDoctor.Enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_email")
    )
    @Column(name = "role") // Store enum name (not ordinal) in DB
    private List<Role> roles; // e.g., [USER, ADMIN]
    private int otp;
}
