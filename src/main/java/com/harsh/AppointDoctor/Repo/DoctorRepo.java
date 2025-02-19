package com.harsh.AppointDoctor.Repo;


import com.harsh.AppointDoctor.Models.Doctors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepo extends JpaRepository<Doctors,String> {
    Doctors findByEmail(String email);
}
