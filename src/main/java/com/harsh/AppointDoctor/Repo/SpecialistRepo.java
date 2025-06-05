package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Models.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialistRepo extends JpaRepository<Specialist,Integer> {
    List<Specialist> findAllByOrderBySpecialistAsc();
}
