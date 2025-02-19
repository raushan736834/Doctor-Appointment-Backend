package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.UsersProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepo extends JpaRepository<UsersProfile,String> {
    UsersProfile findByEmail(String email);
}
