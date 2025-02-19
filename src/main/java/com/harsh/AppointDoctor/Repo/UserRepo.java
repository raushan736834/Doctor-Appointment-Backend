package com.harsh.AppointDoctor.Repo;


import com.harsh.AppointDoctor.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users,String> {
    Users findByEmail(String email);
}
