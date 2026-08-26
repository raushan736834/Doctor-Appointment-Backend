package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLocationRepo extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByUserId(Long userId);
}
