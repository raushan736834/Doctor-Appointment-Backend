package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.OperatingHoursResponse;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Qualification;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.DoctorProfileRepo;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DoctorProfileService {


    private final DoctorProfileRepo doctorRepository;
    private final DoctorRepo doctorRepo;
    private final SpecialistRepo specialistRepo;

    public DoctorProfile saveDoctor(DoctorProfile doctor) {
        for (Qualification q : doctor.getQualifications()) {
            q.setDoctor(doctor);
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

    public List<Doctor> searchDoctorDetails(String keyword) {
        return doctorRepo.searchDoctorDetails(keyword);
    }

    public DoctorProfile getDoctorBySpecializationAndId(DoctorProfile doctor) {
        return doctorRepository.findBySpecializationAndIdIgnoreCase(doctor.getSpecialization(),doctor.getId());
    }

    public Doctor getDoctorById(String doctorId) {
        return doctorRepo.findByDoctorIdIgnoreCase(doctorId);
    }

    public List<String> getAllSpecialization() {
        return doctorRepository.findSpecialization();
    }

    public DoctorProfile getDoctorData(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Specialist> saveAllSpecialist(List<Specialist> specialists) {
        return specialistRepo.saveAll(specialists);
    }

    public List<String> getDistinctCities() {
        return doctorRepository.findDistinctCities();
    }

    public List<DoctorProfile> searchDoctorsByCityAndSpecialist(String city, String specialist) {
        return doctorRepository.findBySpecializationAndCityIgnoreCase(specialist,city);
    }

    public List<OperatingHoursResponse> getOperatingHours(String doctorId) {
        return doctorRepository.findOperatingHoursByDoctorId(doctorId);
    }
}

