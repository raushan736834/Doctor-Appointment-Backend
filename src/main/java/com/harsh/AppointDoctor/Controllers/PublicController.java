package com.harsh.AppointDoctor.Controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.harsh.AppointDoctor.DTOs.ApiResponse;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorOnboardingRequest;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.OperatingHoursResponse;
import com.harsh.AppointDoctor.DTOs.NearbyDoctorRequest;
import com.harsh.AppointDoctor.Models.ContactUs;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorProfile;
import com.harsh.AppointDoctor.Models.Specialist;
import com.harsh.AppointDoctor.Models.UserLocation;
import com.harsh.AppointDoctor.Repo.SpecialistRepo;
import com.harsh.AppointDoctor.Repo.UserLocationRepo;
import com.harsh.AppointDoctor.Services.*;
import com.harsh.AppointDoctor.Services.DoctorOnboardingService.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/public/")
@Slf4j
public class PublicController {

    private final UserAppointmentService appointmentService;
    private final RedisService redisService;
    private final DoctorProfileService doctorService;
    private final ContactUsService contactUsService;
    private final SpecialistRepo repo;
    private final DoctorService doctorServices;
    private final UserLocationRepo userLocationRepo;
    private final LocationService locationService;


    @GetMapping("/getSpecialist")
    public ResponseEntity<ApiResponse<?>> getAllSpecialist() {

        try {
            List<Specialist> specialists =
                    redisService.get("all_specialist", new TypeReference<List<Specialist>>() {});

            if (specialists != null) {
                return ResponseEntity.ok(ApiResponse.success(specialists, "", 200));
            }
        } catch (Exception e) {
            log.error("Redis failed: {}", e.getMessage());
        }

        List<Specialist> data = repo.findAllByOrderBySpecialistAsc();

        try {
            redisService.set("all_specialist", data, 300L);
        } catch (Exception e) {
            log.error("Redis set failed: {}", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(data, "", 200));
    }



    @PostMapping("/nearby")
    public ResponseEntity<?> findNearbyDoctors(@RequestBody NearbyDoctorRequest request) {

        UserLocation location = new UserLocation();
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setUpdatedAt(LocalDateTime.now());

        userLocationRepo.save(location);

        // Now search doctors near this location
        String city = locationService.reverseGeocode(request.getLatitude(), request.getLongitude());
        List<DoctorProfile> doctors = doctorService.searchDoctorsByCityAndSpecialist(city, request.getSpecialist());
        return ResponseEntity.ok(ApiResponse.success(doctors, "", 200));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> getDoctorDetails(@RequestParam String keyword){
        List<Doctor> doctors = null;
        try {
            doctors = doctorService.searchDoctorDetails(keyword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(ApiResponse.success(doctors, "Doctors fetched successfully", 200), HttpStatus.OK);
    }

    @GetMapping("/searchByCityAndSpecialist")
    public ResponseEntity<ApiResponse<?>> getDoctorByCityAndSpecialist(@RequestParam String city, @RequestParam String specialist){
        try {
            List<DoctorProfile> doctors = doctorService.searchDoctorsByCityAndSpecialist(city, specialist);
            return ResponseEntity.ok(ApiResponse.success(doctors,"",200));
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(),500),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/booked-slots")
    public ResponseEntity<ApiResponse<List<LocalTime>>> getBookedSlots(@RequestParam String doctorId,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                          LocalDate date) {
        List<LocalTime> bookedSlots = appointmentService.getBookedSlots(doctorId,date);
        return ResponseEntity.ok(ApiResponse.success(bookedSlots,"",200));
    }

    @PostMapping("/contact-us")
    public ResponseEntity<ApiResponse<?>> contactUs(@RequestBody ContactUs details){
        try{
            details = contactUsService.contactUs(details);
            if (details != null){
                return new ResponseEntity<>(ApiResponse.success(null,"Message Received",200),HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getDoctor/{doctorId}")
    public ResponseEntity<ApiResponse<?>> getDoctorDetailById(@PathVariable String doctorId){
        Doctor doctorDetails = doctorService.getDoctorById(doctorId);
        if (doctorDetails != null) {
            return new ResponseEntity<>(ApiResponse.success(doctorDetails,"",200), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ApiResponse.error("Doctor not found",404), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<?>> getCities(){
        try {
            List<String> cities = doctorService.getDistinctCities();
            return new ResponseEntity<>(ApiResponse.success(cities,"",200),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/operatingHours/{doctorId}")
    public ResponseEntity<ApiResponse<?>> getOperatingHours(@PathVariable String doctorId){
        try {
            List<OperatingHoursResponse> hours = doctorService.getOperatingHours(doctorId);
            return new ResponseEntity<>(ApiResponse.success(hours,"",200),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/onboarding/bulk")
    public ResponseEntity<?> saveBulk(@RequestBody List<DoctorOnboardingRequest> requests) {
        doctorServices.saveDoctorList(requests);
        return ResponseEntity.ok("Bulk doctor save completed successfully");
    }
}
