package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorReviewResponseDTO;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.RatingRequestDTO;
import com.harsh.AppointDoctor.Enums.AppointmentStatus;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.Doctor;
import com.harsh.AppointDoctor.Models.DoctorOnboarding.DoctorRating;
import com.harsh.AppointDoctor.Models.Users;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRatingRepo;
import com.harsh.AppointDoctor.Repo.DoctorOnboardingRepo.DoctorRepo;
import com.harsh.AppointDoctor.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorRatingService {

    private final AppointmentBookingRepo appointmentRepo;
    private final DoctorRatingRepo ratingRepo;
    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;

    @Transactional
    public void submitRating(RatingRequestDTO dto, String userEmail) {

        Users user = userRepo.findByEmail(userEmail);

        AppointmentBooking appointment =
                appointmentRepo.findById(dto.getAppointmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getEmail().equals(userEmail)) {
            throw new AccessDeniedException("This appointment does not belong to you");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Appointment not completed yet");
        }

        Doctor doctor = appointment.getSlot().getDoctor();
        if (doctor.getEmail().equals(userEmail)) {
            throw new IllegalStateException("Doctors cannot rate themselves");
        }

        if (ratingRepo.existsByAppointment(appointment)) {
            throw new IllegalStateException("Rating already submitted");
        }

        DoctorRating rating = new DoctorRating();
        rating.setDoctor(doctor);
        rating.setUser(user);
        rating.setAppointment(appointment);
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());
        rating.setAnonymous(dto.isAnonymous());
        rating.setCreatedAt(LocalDateTime.now());

        ratingRepo.save(rating);

        updateDoctorAggregateRating(doctor, dto.getRating());
    }


    private void updateDoctorAggregateRating(Doctor doctor, int newRating) {

        int total = doctor.getTotalRatings();
        double avg = doctor.getAverageRating();

        double updatedAvg = ((avg * total) + newRating) / (total + 1);

        doctor.setTotalRatings(total + 1);
        doctor.setAverageRating(
                Math.round(updatedAvg * 10.0) / 10.0 // 1 decimal
        );

        doctorRepo.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorReviewResponseDTO> getDoctorReviews(String doctorId) {

        return ratingRepo
                .findByDoctor_DoctorIdOrderByCreatedAtDesc(doctorId)
                .stream()
                .map(r -> new DoctorReviewResponseDTO(
                        r.isAnonymous()
                                ? "Anonymous"
                                : r.getUser().getFirstName(),
                        r.getRating(),
                        r.getReview(),
                        r.getCreatedAt()
                ))
                .toList();
    }
}

