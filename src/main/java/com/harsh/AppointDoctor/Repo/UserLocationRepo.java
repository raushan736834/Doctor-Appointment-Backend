package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLocationRepo extends JpaRepository<UserLocation, Long> {
}
