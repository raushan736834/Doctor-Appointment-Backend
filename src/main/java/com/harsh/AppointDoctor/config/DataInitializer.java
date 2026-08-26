package com.harsh.AppointDoctor.config;

import com.harsh.AppointDoctor.Enums.Role;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEFAULT_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${DEFAULT_ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${DEFAULT_USER_EMAIL}")
    private String userEmail;

    @Value("${DEFAULT_USER_PASSWORD}")
    private String userPassword;

    @Override
    public void run(String... args) {

        createUserIfNotExists(
                userEmail,
                "Default",
                "User",
                userPassword,
                Role.USER
        );

        createUserIfNotExists(
                adminEmail,
                "System",
                "Admin",
                adminPassword,
                Role.ADMIN
        );
    }

    private void createUserIfNotExists(
            String email,
            String firstName,
            String lastName,
            String password,
            Role role
    ) {

        if (userRepo.existsById(email)) {
            return;
        }

        Users user = new Users();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of(role));
        user.setOtp(0);

        userRepo.save(user);

        System.out.println("Created default account: " + email);
    }
}