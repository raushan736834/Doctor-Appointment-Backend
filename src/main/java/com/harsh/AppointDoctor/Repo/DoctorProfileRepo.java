package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.OperatingHoursResponse;
import com.harsh.AppointDoctor.Models.City;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorProfileRepo extends JpaRepository<DoctorProfile,String> {
    @Query("SELECT d from DoctorProfile d WHERE "+
            "LOWER(d.doctorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
            "LOWER(d.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DoctorProfile> searchDoctor(String keyword);

//    @Query("SELECT d from Doctor d WHERE "+
//            "LOWER(d.doctorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
//            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
//            "LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "+
//            "LOWER(d.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//    List<Doctor> searchDoctorsFromDoctor(String keyword);

    @Query("SELECT d FROM DoctorProfile d WHERE LOWER(d.specialization) = LOWER(:specialization) AND LOWER(d.city) = LOWER(:city)")
    List<DoctorProfile> findBySpecializationAndCityIgnoreCase(
            @Param("specialization") String specialization,
            @Param("city") String city);

    @Query("SELECT d FROM DoctorProfile d WHERE LOWER(d.specialization) = LOWER(:specialization) AND LOWER(d.id) = LOWER(:id)")
    DoctorProfile findBySpecializationAndIdIgnoreCase(String specialization, String id);

    @Query("SELECT DISTINCT d.specialization FROM DoctorProfile d")
    List<String> findSpecialization();

    DoctorProfile findByEmail(String email);
    List<DoctorProfile> findByCity(City city);

    @Query("SELECT DISTINCT d.city FROM DoctorProfile d")
    List<String> findDistinctCities();

    @Query("""
    SELECT new com.harsh.AppointDoctor.DTOs.DoctorDTOs.OperatingHoursResponse(
        oh.days, oh.open, oh.close, oh.isClosedToday
    )
    FROM Doctor d
    JOIN d.clinicInfos ci
    JOIN ci.operatingHours oh
    WHERE d.doctorId = :doctorId
    ORDER BY CASE oh.days
            WHEN com.harsh.AppointDoctor.Enums.Days.MONDAY THEN 1
            WHEN com.harsh.AppointDoctor.Enums.Days.TUESDAY THEN 2
            WHEN com.harsh.AppointDoctor.Enums.Days.WEDNESDAY THEN 3
            WHEN com.harsh.AppointDoctor.Enums.Days.THURSDAY THEN 4
            WHEN com.harsh.AppointDoctor.Enums.Days.FRIDAY THEN 5
            WHEN com.harsh.AppointDoctor.Enums.Days.SATURDAY THEN 6
            WHEN com.harsh.AppointDoctor.Enums.Days.SUNDAY THEN 7
        END
""")
    List<OperatingHoursResponse> findOperatingHoursByDoctorId(@Param("doctorId") String doctorId);


}