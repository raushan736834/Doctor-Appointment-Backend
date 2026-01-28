package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.DoctorDTOs.DoctorReviewResponseDTO;
import com.harsh.AppointDoctor.DTOs.DoctorDTOs.RatingRequestDTO;
import com.harsh.AppointDoctor.Services.DoctorRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class DoctorRatingController {

    private final DoctorRatingService ratingService;

    @PostMapping
    public ResponseEntity<?> submitRating(
            @RequestBody RatingRequestDTO dto,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ratingService.submitRating(dto, userEmail);
        return ResponseEntity.ok("Rating submitted successfully");
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorReviewResponseDTO>> getDoctorReviews(
            @PathVariable String doctorId
    ) {
        return ResponseEntity.ok(
                ratingService.getDoctorReviews(doctorId)
        );
    }
}
