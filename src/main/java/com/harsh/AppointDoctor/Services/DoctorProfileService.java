package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Qualification;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorProfileService {

    @Autowired
    private DoctorProfileRepo doctorRepository;

    public DoctorProfile saveDoctor(DoctorProfile doctor) {
        for (Qualification q : doctor.getQualifications()) {
            q.setDoctor(doctor); // Important for bidirectional mapping
        }
        return doctorRepository.save(doctor);
    }

    public List<DoctorProfile> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<DoctorProfile> saveAllDoctors(List<DoctorProfile> doctors) {
        for (DoctorProfile doctor : doctors) {
            doctor.getQualifications().forEach(q -> q.setDoctor(doctor)); // link back
        }
        return doctorRepository.saveAll(doctors);
    }

    public List<DoctorProfile> searchDoctors(String keyword) {
        return doctorRepository.searchDoctor(keyword);
    }

    public DoctorProfile getDoctorBySpecializationAndId(DoctorProfile doctor) {
        return doctorRepository.findBySpecializationAndIdIgnoreCase(doctor.getSpecialization(),doctor.getId());
    }
}

